// ===== 포트폴리오 필터/검색 =====
document.addEventListener('DOMContentLoaded', () => {
  const tabs = document.querySelectorAll('.portfolio__tab');
  const searchInput = document.querySelector('.portfolio__search');
  const grid = document.querySelector('.portfolio__grid');
  const items = document.querySelectorAll('.portfolio__item');
  const emptyMsg = document.querySelector('.portfolio__empty');
  const videoGroups = document.querySelector('.portfolio__video-groups');
  const videoGroupList = document.querySelectorAll('.portfolio__video-group');

  if (!tabs.length) return; // 포트폴리오 페이지가 아니면 아무것도 안 함

  let activeFilter = 'all';

  function applyFilters() {
    const keyword = searchInput.value.trim().toLowerCase();

    if (activeFilter === 'video') {
      grid.style.display = 'none';
      emptyMsg.hidden = true;
      videoGroups.classList.add('is-visible');

      videoGroupList.forEach((group) => {
        const cardsInGroup = group.querySelectorAll('.portfolio__video-card');
        let visibleInGroup = 0;
        cardsInGroup.forEach((card) => {
          const match = card.dataset.name.toLowerCase().includes(keyword);
          card.style.display = match ? '' : 'none';
          if (match) visibleInGroup++;
        });
        group.classList.toggle('is-hidden', visibleInGroup === 0);
      });
      return;
    }

    grid.style.display = '';
    videoGroups.classList.remove('is-visible');

    let visibleCount = 0;
    items.forEach((item) => {
      const matchesCategory = activeFilter === 'all' || item.dataset.category === activeFilter;
      const matchesKeyword = item.dataset.name.toLowerCase().includes(keyword);
      const show = matchesCategory && matchesKeyword;
      item.classList.toggle('is-hidden', !show);
      if (show) visibleCount++;
    });
    emptyMsg.hidden = visibleCount > 0;
  }

  tabs.forEach((tab) => {
    tab.addEventListener('click', () => {
      tabs.forEach((t) => t.classList.remove('is-active'));
      tab.classList.add('is-active');
      activeFilter = tab.dataset.filter;
      applyFilters();
    });
  });

  searchInput.addEventListener('input', applyFilters);

  // 전체 탭의 영상 카드를 누르면 "영상" 탭으로 전환하고 해당 구역으로 이동
  document.querySelectorAll('.portfolio__item--video').forEach((item) => {
    item.addEventListener('click', (e) => {
      e.preventDefault();
      const videoTab = document.querySelector('.portfolio__tab[data-filter="video"]');
      if (videoTab) videoTab.click();
      const target = document.getElementById(item.dataset.target);
      if (target) target.scrollIntoView({ behavior: 'smooth', block: 'start' });
    });
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
      resultMsg.textContent = '신청이 접수되었습니다. 확인 후 연락드리겠습니다.';
      resultMsg.className = 'apply-form__result is-success';
      resultMsg.style.display = 'block';
      applyForm.reset();
      updateVisibleFields(getSelectedServiceType());
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