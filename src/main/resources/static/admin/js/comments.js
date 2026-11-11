/* 직원 댓글형 메모 (GIF 신청 / 서비스 문의 공용)
 *  사용: initComments(box, 'gif' | 'inquiry', 대상ID, 변경후콜백(선택), { rich: true(선택) })
 *  rich: true 이면 글자 강조/색/크기를 쓸 수 있는 에디터(Quill)를 사용한다. (Quill 라이브러리가 페이지에 로드되어 있어야 함)
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

function initComments(box, type, targetId, onChange, options) {
	const rich = !!(options && options.rich) && typeof Quill !== 'undefined';

	const TOOLBAR = [
		[{ size: ['small', false, 'large', 'huge'] }],
		['bold', 'italic', 'underline', 'strike'],
		[{ color: [] }, { background: [] }],
		['clean']
	];

	function createEditor(container, initialHtml, placeholder) {
		container.innerHTML = '<div class="cmt-quill"></div>';
		const q = new Quill(container.querySelector('.cmt-quill'), {
			theme: 'snow',
			placeholder: placeholder || '',
			modules: { toolbar: TOOLBAR }
		});
		if (initialHtml) q.clipboard.dangerouslyPasteHTML(initialHtml);
		return q;
	}

	box.innerHTML = `
		<div class="cmt-list" data-role="list"><p class="text-muted small mb-0">불러오는 중...</p></div>
		<div class="cmt-form">
			${rich
				? '<div data-role="editor"></div>'
				: '<textarea class="form-control" rows="3" maxlength="5000" placeholder="메모를 입력하세요" data-role="input"></textarea>'}
			<div class="text-end mt-2">
				<button type="button" class="btn btn-primary btn-sm" data-role="submit">등록</button>
			</div>
		</div>
	`;
	const listEl = box.querySelector('[data-role="list"]');
	const submitEl = box.querySelector('[data-role="submit"]');
	const inputEl = box.querySelector('[data-role="input"]');
	let quill = null;
	if (rich) quill = createEditor(box.querySelector('[data-role="editor"]'), '', '메모를 입력하세요');

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

	function bodyHtml(c) {
		// 서식 있는 글(html)은 서버에서 허용된 서식만 남기고 저장된 것
		return c.format === 'html' ? c.content : escapeHtml(c.content);
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
				<div class="cmt-body${c.format === 'html' ? ' cmt-body--html' : ''}">${bodyHtml(c)}</div>
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
		if (typeof onChange === 'function') onChange();
	}

	submitEl.addEventListener('click', async () => {
		let content;
		if (rich) {
			if (!quill.getText().trim()) { quill.focus(); return; }
			content = quill.root.innerHTML;
		} else {
			content = inputEl.value.trim();
			if (!content) { inputEl.focus(); return; }
		}
		submitEl.disabled = true;
		try {
			const res = await fetch('/api/admin/comments', {
				method: 'POST',
				headers: { 'Content-Type': 'application/json' },
				body: JSON.stringify({ type, id: targetId, content, format: rich ? 'html' : 'text' })
			});
			if (!res.ok) { alert(await readError(res)); return; }
			if (rich) quill.setContents([]); else inputEl.value = '';
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
			if (!current) return;
			const bodyEl = itemEl.querySelector('.cmt-body');
			const isHtml = current.format === 'html' && rich;

			bodyEl.innerHTML = `
				<div data-role="edit-field"></div>
				<div class="text-end mt-1">
					<button type="button" class="btn btn-primary btn-sm" data-edit="save">저장</button>
					<button type="button" class="btn btn-outline-secondary btn-sm" data-edit="cancel">취소</button>
				</div>`;
			const fieldEl = bodyEl.querySelector('[data-role="edit-field"]');
			let editQuill = null;
			let ta = null;
			if (isHtml) {
				editQuill = createEditor(fieldEl, current.content, '');
				editQuill.focus();
			} else {
				fieldEl.innerHTML = `<textarea class="form-control form-control-sm" rows="3" maxlength="5000">${escapeHtml(current.content)}</textarea>`;
				ta = fieldEl.querySelector('textarea');
				ta.focus();
			}
			bodyEl.querySelector('[data-edit="cancel"]').addEventListener('click', () => render(listEl._items || []));
			bodyEl.querySelector('[data-edit="save"]').addEventListener('click', async () => {
				let content;
				if (editQuill) {
					if (!editQuill.getText().trim()) { editQuill.focus(); return; }
					content = editQuill.root.innerHTML;
				} else {
					content = ta.value.trim();
					if (!content) { ta.focus(); return; }
				}
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