/* 직원 댓글형 메모 (GIF 신청 / 서비스 문의 공용)
 *  사용: initComments(document.getElementById('box'), 'gif' | 'inquiry', 대상ID)
 */
function escapeHtml(value) {
	if (value === null || value === undefined) return '';
	return String(value)
		.replace(/&/g, '&amp;')
		.replace(/</g, '&lt;')
		.replace(/>/g, '&gt;')
		.replace(/"/g, '&quot;')
		.replace(/'/g, '&#39;');
}

function initComments(box, type, targetId) {
	box.innerHTML = `
		<div class="cmt-list" data-role="list"><p class="text-muted small mb-0">불러오는 중...</p></div>
		<div class="cmt-form">
			<textarea class="form-control" rows="3" maxlength="5000" placeholder="메모를 입력하세요" data-role="input"></textarea>
			<div class="text-end mt-2">
				<button type="button" class="btn btn-primary btn-sm" data-role="submit">등록</button>
			</div>
		</div>
	`;
	const listEl = box.querySelector('[data-role="list"]');
	const inputEl = box.querySelector('[data-role="input"]');
	const submitEl = box.querySelector('[data-role="submit"]');

	function fmt(dateStr) {
		return dateStr ? dateStr.replace('T', ' ').substring(0, 16) : '';
	}

	async function readError(res) {
		try {
			const data = await res.json();
			return data.message || data.error || '요청에 실패했습니다.';
		} catch (e) {
			return '요청에 실패했습니다.';
		}
	}

	function render(items) {
		if (!items.length) {
			listEl.innerHTML = '<p class="text-muted small mb-0">아직 남겨진 메모가 없습니다.</p>';
			return;
		}
		listEl.innerHTML = items.map(c => `
			<div class="cmt-item" data-id="${c.id}">
				<div class="cmt-head">
					<span class="cmt-author">${escapeHtml(c.authorName || '알 수 없음')}</span>
					<span class="cmt-time">${fmt(c.createdAt)}${c.updatedAt ? ' (수정됨)' : ''}</span>
					${c.mine ? `<span class="cmt-actions">
						<a href="#" data-act="edit">수정</a>
						<a href="#" data-act="delete" class="text-danger">삭제</a>
					</span>` : ''}
				</div>
				<div class="cmt-body">${escapeHtml(c.content)}</div>
			</div>
		`).join('');
		listEl.scrollTop = listEl.scrollHeight;
	}

	async function load() {
		const res = await fetch(`/api/admin/comments?type=${encodeURIComponent(type)}&id=${targetId}`);
		if (!res.ok) { listEl.innerHTML = '<p class="text-danger small mb-0">메모를 불러오지 못했습니다.</p>'; return; }
		const items = await res.json();
		listEl._items = items;
		render(items);
	}

	submitEl.addEventListener('click', async () => {
		const content = inputEl.value.trim();
		if (!content) { inputEl.focus(); return; }
		submitEl.disabled = true;
		try {
			const res = await fetch('/api/admin/comments', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ type, id: targetId, content })
			});
			if (!res.ok) { alert(await readError(res)); return; }
			inputEl.value = '';
			await load();
		} finally {
			submitEl.disabled = false;
		}
	});

	listEl.addEventListener('click', async (e) => {
		const link = e.target.closest('a[data-act]');
		if (!link) return;
		e.preventDefault();
		const itemEl = link.closest('.cmt-item');
		const id = itemEl.dataset.id;
		const act = link.dataset.act;

		if (act === 'delete') {
			if (!confirm('이 메모를 삭제하시겠습니까?')) return;
			const res = await fetch(`/api/admin/comments/${id}`, { method: 'DELETE' });
			if (!res.ok) { alert(await readError(res)); return; }
			await load();
		} else if (act === 'edit') {
			const current = (listEl._items || []).find(c => String(c.id) === String(id));
			const bodyEl = itemEl.querySelector('.cmt-body');
			bodyEl.innerHTML = `
				<textarea class="form-control form-control-sm" rows="3" maxlength="5000">${escapeHtml(current ? current.content : '')}</textarea>
				<div class="text-end mt-1">
					<button type="button" class="btn btn-primary btn-sm" data-edit="save">저장</button>
					<button type="button" class="btn btn-outline-secondary btn-sm" data-edit="cancel">취소</button>
				</div>`;
			const ta = bodyEl.querySelector('textarea');
			ta.focus();
			bodyEl.querySelector('[data-edit="cancel"]').addEventListener('click', () => render(listEl._items || []));
			bodyEl.querySelector('[data-edit="save"]').addEventListener('click', async () => {
				const content = ta.value.trim();
				if (!content) { ta.focus(); return; }
				const res = await fetch(`/api/admin/comments/${id}`, {
					method: 'PUT',
					headers: { 'Content-Type': 'application/json' },
					body: JSON.stringify({ content })
				});
				if (!res.ok) { alert(await readError(res)); return; }
				await load();
			});
		}
	});

	load();
}