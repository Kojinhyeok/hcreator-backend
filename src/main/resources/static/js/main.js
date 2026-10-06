// ===== 포트폴리오 - 탭/서브탭 전환 + 이미지 모달 =====
document.addEventListener('DOMContentLoaded', () => {
  const tabs = document.querySelectorAll('.portfolio__tab');
  if (!tabs.length) return; // 포트폴리오 페이지가 아니면 아무것도 안 함

  tabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      tabs.forEach((t) => t.classList.remove('is-active'));
      tab.classList.add('is-active');
      const target = tab.dataset.panel;
      document.querySelectorAll('.portfolio__panel').forEach((panel) => {
        panel.classList.toggle('is-active', panel.dataset.panel === target);
      });
    });
  });

  const subtabs = document.querySelectorAll('.portfolio__subtab');
  subtabs.forEach((subtab) => {
    subtab.addEventListener('click', () => {
      subtabs.forEach((t) => t.classList.remove('is-active'));
      subtab.classList.add('is-active');
      const target = subtab.dataset.sub;
      document.querySelectorAll('.portfolio__subpanel').forEach((panel) => {
        panel.classList.toggle('is-active', panel.dataset.sub === target);
      });
    });
  });

  // 이미지 확대 모달 (AI이미지 / 카드뉴스 / 상세페이지 공용)
  const modal = document.getElementById('portfolioModal');
  const modalImg = document.getElementById('portfolioModalImg');
  const modalName = document.getElementById('portfolioModalName');
  const modalBackdrop = document.getElementById('portfolioModalBackdrop');
  const modalClose = document.getElementById('portfolioModalClose');
  const modalPanel = document.getElementById('portfolioModalPanel');
  const modalContent = document.getElementById('portfolioModalContent');
  let openToken = 0; // 늦게 도착한 응답이 다른 항목 위에 덮어쓰이지 않게 하는 용도

  function showSingleImage(card) {
    modalContent.classList.remove('is-active');
    modalContent.innerHTML = '';
    modalImg.style.display = '';
    modalImg.src = card.dataset.img;
  }

  // 상세페이지: 에디터로 이어붙인 내용을 클릭할 때만 불러온다
  function showDetail(card, token) {
    modalImg.style.display = 'none';
    modalContent.classList.add('is-active');
    modalContent.innerHTML = '<p class="portfolio-modal__loading">불러오는 중...</p>';

    fetch(`/api/portfolio/items/${card.dataset.id}`)
      .then((res) => {
        if (!res.ok) throw new Error('load failed');
        return res.json();
      })
      .then((data) => {
        if (token !== openToken) return;
        const html = data.detailContent && data.detailContent.trim() ? data.detailContent : '';
        if (!html) { showSingleImage(card); return; }
        modalContent.innerHTML = html;
        if (window.initLazyVideos) window.initLazyVideos(modalContent); // 영상은 화면에 보일 때만 재생
      })
      .catch(() => {
        if (token !== openToken) return;
        showSingleImage(card); // 실패하면 대표 이미지라도 보여준다
      });
  }

  document.querySelectorAll('.portfolio__card').forEach((card) => {
    card.addEventListener('click', (e) => {
      e.preventDefault();
      const token = ++openToken;
      modalName.textContent = card.dataset.name || '';
      modalPanel.scrollTop = 0;

      if (card.dataset.detail === 'true' && card.dataset.id) {
        showDetail(card, token);
      } else {
        showSingleImage(card);
      }

      modal.classList.add('is-open');
      document.body.style.overflow = 'hidden';
    });
  });

  function closeModal() {
    openToken++; // 진행 중인 불러오기 무효화
    modal.classList.remove('is-open');
    modalContent.innerHTML = '';                 // 닫으면 영상도 함께 정리
    modalContent.classList.remove('is-active');
    document.body.style.overflow = '';
  }

  modalBackdrop.addEventListener('click', closeModal);
  modalClose.addEventListener('click', closeModal);
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
  });
});

// ===== 서비스 신청 폼 (GIF / 상세페이지 / 블로그 / 영상) =====
document.addEventListener('DOMContentLoaded', () => {
  const applyForm = document.getElementById('applyForm');
  if (!applyForm) return; // 신청서 페이지가 아니면 종료

  const typeRadios = applyForm.querySelectorAll('input[name="serviceType"]');
  const fieldsets = applyForm.querySelectorAll('[data-fieldset]');
  const gifTableBody = document.getElementById('gifRequestTableBody');
  const addGifRowBtn = document.getElementById('addGifRow');

  // 신청 완료 팝업
  const applySuccessModal = document.getElementById('applySuccessModal');
  const applySuccessBackdrop = document.getElementById('applySuccessBackdrop');
  const applySuccessClose = document.getElementById('applySuccessClose');

  function openSuccessModal() {
    applySuccessModal.classList.add('is-open');
  }

  function closeSuccessModal() {
    applySuccessModal.classList.remove('is-open');
  }

  if (applySuccessBackdrop) applySuccessBackdrop.addEventListener('click', closeSuccessModal);
  if (applySuccessClose) applySuccessClose.addEventListener('click', closeSuccessModal);

  function getSelectedServiceType() {
    const checked = applyForm.querySelector('input[name="serviceType"]:checked');
    return checked ? checked.value : 'gif';
  }

  function updateVisibleFields(type) {
    fieldsets.forEach((el) => {
      const types = el.dataset.fieldset.split(',');
      const hidden = !types.includes(type);
      el.classList.toggle('is-hidden', hidden);
      // display:none 만으로는 전송을 막지 못한다.
      // 선택하지 않은 구분의 값이 함께 전송되지 않도록 disabled 처리한다.
      el.querySelectorAll('input, select, textarea').forEach((control) => {
        control.disabled = hidden;
      });
    });
  }

  typeRadios.forEach((radio) => {
    radio.addEventListener('change', () => {
      if (radio.checked) updateVisibleFields(radio.value);
    });
  });

  updateVisibleFields(getSelectedServiceType()); // 초기 상태 반영

  // GIF 신청 테이블 - 행 추가/삭제
  function renumberGifRows() {
    gifTableBody.querySelectorAll('tr').forEach((row, index) => {
      row.querySelector('.apply-form__table-num').textContent = index + 1;
    });
  }

  if (addGifRowBtn) {
    addGifRowBtn.addEventListener('click', () => {
      const newRow = document.createElement('tr');
      newRow.innerHTML = `
        <td class="apply-form__table-num"></td>
        <td><input type="text" name="reportNo[]" class="apply-form__table-input"></td>
        <td><input type="text" name="testItem[]" class="apply-form__table-input"></td>
        <td><input type="text" name="templateNo[]" class="apply-form__table-input"></td>
        <td><input type="text" name="subjectNo[]" class="apply-form__table-input"></td>
        <td><button type="button" class="apply-form__table-remove">삭제</button></td>
      `;
      gifTableBody.appendChild(newRow);
      renumberGifRows();
    });

    gifTableBody.addEventListener('click', (e) => {
      if (e.target.classList.contains('apply-form__table-remove')) {
        const rows = gifTableBody.querySelectorAll('tr');
        if (rows.length > 1) {
          e.target.closest('tr').remove();
          renumberGifRows();
        }
      }
    });
  }

  // 파일 하나를 S3에 올리고 접근 가능한 URL을 반환
  async function uploadFile(file, folder) {
    if (!file) return null;
    const presign = await fetch('/api/uploads/presign', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fileName: file.name, folder })
    }).then((res) => res.json());

    await fetch(presign.uploadUrl, { method: 'PUT', body: file, headers: { 'Content-Type': file.type } });
    return presign.publicUrl;
  }

  async function submitGifApplication(form) {
    const fileInput = document.getElementById('gifFile');
    const filePath = await uploadFile(fileInput?.files[0], 'apply/gif');

    const rows = [];
    gifTableBody.querySelectorAll('tr').forEach((row) => {
      const inputs = row.querySelectorAll('.apply-form__table-input');
      rows.push({
        reportNo: inputs[0]?.value || '',
        testItem: inputs[1]?.value || '',
        templateNo: inputs[2]?.value || '',
        subjectNo: inputs[3]?.value || ''
      });
    });

    const body = {
      companyName: form.companyName.value,
      contactName: form.contactName.value,
      contactPhone: form.contactPhone.value,
      contactEmail: form.contactEmail.value,
      pointColor: form.pointColor.value,
      bgColor: form.bgColor.value,
      bgEffect: form.bgEffect.value,
      quantity: form.gifQuantity.value,
      filePath,
      etcNote: form.etcNote.value,
      rows
    };

    const res = await fetch('/api/apply/gif', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!res.ok) throw new Error('submit failed');
  }

  async function submitServiceApplication(form, serviceType) {
    let attachmentPath = null;
    if (serviceType === 'blog') {
      attachmentPath = await uploadFile(document.getElementById('blogFile')?.files[0], 'apply/blog');
    }

    const videoFormat = Array.from(form.querySelectorAll('input[name="videoFormat"]:checked'))
      .map((el) => el.value).join(',');
    const videoContent = Array.from(form.querySelectorAll('input[name="videoContent"]:checked'))
      .map((el) => el.value).join(',');

    const body = {
      serviceType,
      companyName: form.companyName.value,
      contactName: form.contactName.value,
      contactPhone: form.contactPhone.value,
      contactEmail: form.contactEmail.value,
      productName: serviceType === 'detail-page'
        ? (form.productName?.value || null)
        : (form.productNameAlt?.value || null),
      brandName: form.brandName?.value || null,
      launchDate: form.launchDate?.value || null,
      needsShooting: form.needsShooting?.value || null,
      hasPlan: form.hasPlan?.value || null,
      attachmentPath,
      promoContent: form.promoContent?.value || null,
      videoFormat: videoFormat || null,
      videoContent: videoContent || null,
      etcNote: form.etcNote?.value || null,
      requestNote: form.requestNote?.value || null
    };

    const res = await fetch('/api/apply/service', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body)
    });
    if (!res.ok) throw new Error('submit failed');
  }

  applyForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const resultMsg = document.getElementById('applyResultMsg');
    const submitBtn = applyForm.querySelector('.apply-form__submit');
    const serviceType = getSelectedServiceType(); // 매번 현재 체크된 라디오에서 직접 읽음

    resultMsg.style.display = 'none';
    submitBtn.disabled = true;
    submitBtn.textContent = '전송 중...';

    try {
      if (serviceType === 'gif') {
        await submitGifApplication(applyForm);
      } else {
        await submitServiceApplication(applyForm, serviceType);
      }
      applyForm.reset();
      updateVisibleFields(getSelectedServiceType());
      openSuccessModal();
    } catch (err) {
      resultMsg.textContent = '전송에 실패했습니다. 잠시 후 다시 시도해주세요.';
      resultMsg.className = 'apply-form__result is-error';
      resultMsg.style.display = 'block';
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = '신청하기';
    }
  });
});

// ===== 서비스 문의 폼 =====
document.addEventListener('DOMContentLoaded', () => {
  const inquiryForm = document.getElementById('inquiryForm');
  if (!inquiryForm) return; // 문의 페이지가 아니면 종료

  async function uploadFile(file, folder) {
    if (!file) return null;
    const presign = await fetch('/api/uploads/presign', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ fileName: file.name, folder })
    }).then((res) => res.json());

    await fetch(presign.uploadUrl, { method: 'PUT', body: file, headers: { 'Content-Type': file.type } });
    return presign.publicUrl;
  }

  inquiryForm.addEventListener('submit', async (e) => {
    e.preventDefault();
    const resultMsg = document.getElementById('inquiryResultMsg');
    const submitBtn = inquiryForm.querySelector('.apply-form__submit');
    const consent = document.getElementById('inqConsent');

    if (!consent.checked) return; // required 속성이 이미 막아주지만 이중 방어

    resultMsg.style.display = 'none';
    submitBtn.disabled = true;
    submitBtn.textContent = '전송 중...';

    try {
      const attachmentPath = await uploadFile(document.getElementById('inqFile')?.files[0], 'inquiry');

      const body = {
        companyName: inquiryForm.companyName.value,
        contactName: inquiryForm.contactName.value,
        contactPhone: inquiryForm.contactPhone.value,
        contactEmail: inquiryForm.contactEmail.value,
        inquiryService: inquiryForm.inquiryService.value,
        title: inquiryForm.title.value,
        content: inquiryForm.content.value,
        attachmentPath,
        password: inquiryForm.password.value
      };

      const res = await fetch('/api/inquiry', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body)
      });
      if (!res.ok) throw new Error('submit failed');

      resultMsg.textContent = '문의가 접수되었습니다.';
      resultMsg.className = 'apply-form__result is-success';
      resultMsg.style.display = 'block';
      inquiryForm.reset();
    } catch (err) {
      resultMsg.textContent = '전송에 실패했습니다. 잠시 후 다시 시도해주세요.';
      resultMsg.className = 'apply-form__result is-error';
      resultMsg.style.display = 'block';
    } finally {
      submitBtn.disabled = false;
      submitBtn.textContent = '문의하기';
    }
  });
});

// ===== 모바일 헤더 메뉴 =====
document.addEventListener('DOMContentLoaded', () => {
  const menuBtn = document.getElementById('mobileMenuBtn');
  const mobileNav = document.getElementById('mobileNav');
  if (!menuBtn || !mobileNav) return;

  menuBtn.addEventListener('click', () => {
    const isOpen = mobileNav.classList.toggle('is-open');
    menuBtn.classList.toggle('is-active', isOpen);
    menuBtn.setAttribute('aria-expanded', isOpen ? 'true' : 'false');
  });

  mobileNav.querySelectorAll('a').forEach((link) => {
    link.addEventListener('click', () => {
      mobileNav.classList.remove('is-open');
      menuBtn.classList.remove('is-active');
      menuBtn.setAttribute('aria-expanded', 'false');
    });
  });
});

// ===== GIF 템플릿 상세 모달 (지연 로딩) =====
document.addEventListener('DOMContentLoaded', () => {
  const items = document.querySelectorAll('.gif-gallery__item[data-id]');
  const modal = document.getElementById('gifDetailModal');
  if (!items.length || !modal) return; // 이 페이지에 갤러리가 없으면 종료

  const backdrop = document.getElementById('gifDetailBackdrop');
  const closeBtn = document.getElementById('gifDetailClose');
  const body = document.getElementById('gifDetailBody');

  function openModal() {
    modal.classList.add('is-open');
    document.body.style.overflow = 'hidden';
  }

  function closeModal() {
    modal.classList.remove('is-open');
    document.body.style.overflow = '';
  }

  function loadDetail(id) {
    body.innerHTML = '<p class="gif-detail-modal__loading">불러오는 중...</p>';
    openModal();

    fetch(`/api/gif/${id}`)
      .then((res) => {
        if (!res.ok) throw new Error('load failed');
        return res.json();
      })
      .then((data) => {
        const content = data.detailContent && data.detailContent.trim()
          ? data.detailContent
          : '<p>등록된 상세 내용이 없습니다.</p>';
        body.innerHTML = `<h2>${data.title ?? ''}</h2>${content}`;
        if (window.initLazyVideos) window.initLazyVideos(body); // 상세 내용 안 영상: 화면에 보일 때만 재생
      })
      .catch(() => {
        body.innerHTML = '<p class="gif-detail-modal__loading">불러오지 못했습니다. 잠시 후 다시 시도해주세요.</p>';
      });
  }

  items.forEach((item) => {
    item.addEventListener('click', () => {
      const id = item.dataset.id;
      if (id) loadDetail(id);
    });
  });

  closeBtn.addEventListener('click', closeModal);
  backdrop.addEventListener('click', closeModal);
  document.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') closeModal();
  });
});

// ===== GIF 라벨 — 줄바꿈 기준으로 체크마크 뱃지화 =====
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('.gif-gallery__label').forEach((label) => {
    const lines = label.textContent
      .split(/\r?\n/)
      .map((line) => line.trim())
      .filter(Boolean);

    if (lines.length === 0) return;

    label.innerHTML = lines
      .map((line) => `<span class="gif-gallery__label-item">${line}</span>`)
      .join('');
  });
});

// ===== 포트폴리오 - 검색 + 그리드/영상행 페이지네이션 =====
document.addEventListener('DOMContentLoaded', () => {
  const containers = document.querySelectorAll('.portfolio__grid[data-limit], .portfolio__video-row[data-limit]');
  if (!containers.length) return;

  const searchInput = document.querySelector('.portfolio__search');
  const noResult = document.querySelector('.portfolio__no-result');
  let keyword = '';

  const states = Array.from(containers).map((container) => {
    const isGrid = container.classList.contains('portfolio__grid');
    const itemSelector = isGrid ? '.portfolio__card' : '.portfolio__video-card';
    const labelSelector = isGrid ? '.portfolio__card-label' : '.portfolio__video-title';
    const items = Array.from(container.querySelectorAll(itemSelector));
    return {
      container,
      items,
      names: items.map((el) =>
        (el.dataset.name || el.querySelector(labelSelector)?.textContent || '').toLowerCase()
      ),
      pageSize: parseInt(container.dataset.limit, 10),
      pagination: container.nextElementSibling,
      page: 1,
      matchedCount: 0
    };
  });

  function renderState(state) {
    const matched = state.items.filter((_, i) => state.names[i].includes(keyword));
    const pageCount = Math.max(1, Math.ceil(matched.length / state.pageSize));
    if (state.page > pageCount) state.page = pageCount;

    state.items.forEach((el) => { el.style.display = 'none'; });
    matched
      .slice((state.page - 1) * state.pageSize, state.page * state.pageSize)
      .forEach((el) => { el.style.display = ''; });

    state.matchedCount = matched.length;

    // 영상 행: 검색 결과가 없으면 행과 소제목을 함께 숨김
    const isVideoRow = state.container.classList.contains('portfolio__video-row');
    state.container.style.display = matched.length === 0 && keyword ? 'none' : '';
    if (isVideoRow) {
      const prev = state.container.previousElementSibling;
      if (prev && prev.classList.contains('portfolio__video-sublabel')) {
        prev.style.display = matched.length === 0 && keyword ? 'none' : '';
      }
    }

    // 페이지 번호
    const pagination = state.pagination;
    if (pagination && pagination.classList.contains('portfolio__pagination')) {
      pagination.innerHTML = '';
      if (pageCount > 1) {
        for (let p = 1; p <= pageCount; p++) {
          const btn = document.createElement('button');
          btn.type = 'button';
          btn.className = 'portfolio__page-btn' + (p === state.page ? ' is-active' : '');
          btn.textContent = p;
          btn.addEventListener('click', () => {
            state.page = p;
            renderState(state);
          });
          pagination.appendChild(btn);
        }
      }
    }
  }

  function updateGroupsAndNoResult() {
    // 영상 그룹: 안의 행이 전부 비면 제목(1. 제품 홍보 영상 등)까지 숨김
    document.querySelectorAll('.portfolio__video-group').forEach((group) => {
      const groupStates = states.filter((s) => group.contains(s.container));
      const hasAny = groupStates.some((s) => s.matchedCount > 0);
      group.style.display = keyword && !hasAny ? 'none' : '';
    });

    // 현재 열려 있는 탭 기준으로 결과 없음 문구 표시
    if (noResult) {
      const activePanel = document.querySelector('.portfolio__panel.is-active');
      const total = states
        .filter((s) => activePanel && activePanel.contains(s.container))
        .reduce((sum, s) => sum + s.matchedCount, 0);
      noResult.hidden = !keyword || total > 0;
    }
  }

  function renderAll() {
    states.forEach(renderState);
    updateGroupsAndNoResult();
  }

  if (searchInput) {
    searchInput.addEventListener('input', () => {
      keyword = searchInput.value.trim().toLowerCase();
      states.forEach((s) => { s.page = 1; });
      renderAll();
    });
  }

  // 탭 전환 시 결과 없음 문구 갱신
  document.querySelectorAll('.portfolio__tab').forEach((tab) => {
    tab.addEventListener('click', () => {
      // 탭 전환 로직(is-active 토글)이 먼저 실행된 뒤 갱신되도록 다음 틱에 실행
      setTimeout(updateGroupsAndNoResult, 0);
    });
  });

  renderAll();
});

// ===== 레이지 비디오 (GIF 대체 MP4): 화면에 들어오면 재생, 벗어나면 일시정지 =====
// 페이지 로드 시 자동 적용되고, 동적으로 채운 영역(모달 등)은 window.initLazyVideos(영역)으로 다시 적용
(function () {
  let observer = null;

  function loadVideo(video) {
    if (video.dataset.loaded) return;
    if (video.dataset.src) video.src = video.dataset.src;             // 에디터가 만든 <video data-src>
    video.querySelectorAll('source[data-src]').forEach((source) => {  // 갤러리의 <video><source data-src>
      source.src = source.dataset.src;
    });
    video.load();
    video.dataset.loaded = '1';
  }

  function playVideo(video) {
    const promise = video.play();
    if (promise && promise.catch) promise.catch(() => {}); // 저전력 모드 등으로 막히면 포스터 유지
  }

  function getObserver() {
    if (observer || !('IntersectionObserver' in window)) return observer;
    observer = new IntersectionObserver((entries) => {
      entries.forEach((entry) => {
        const video = entry.target;
        if (entry.isIntersecting) {
          loadVideo(video);
          playVideo(video);
        } else {
          video.pause();
        }
      });
    }, { rootMargin: '200px 0px', threshold: 0.1 });
    return observer;
  }

  window.initLazyVideos = function (root) {
    const videos = (root || document).querySelectorAll('video.lazy-video');
    if (!videos.length) return;
    const io = getObserver();
    videos.forEach((video) => {
      video.muted = true;
      if (io) {
        io.observe(video);
      } else {
        loadVideo(video);
        playVideo(video);
      }
    });
  };

  document.addEventListener('DOMContentLoaded', () => window.initLazyVideos(document));
})();