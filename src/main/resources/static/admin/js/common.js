document.addEventListener("DOMContentLoaded", function () {
	const sidebarEl = document.getElementById("sidebar-container");
	const headerEl = document.getElementById("header-container");
	const base = document.body.dataset.base || "";
	const loads = [];
	var isLoginPage = window.location.pathname.indexOf("/admin/login") >= 0;

	if (sidebarEl) {
		loads.push(
			fetch(base + "components/sidebar.html")
				.then(res => res.text())
				.then(html => {
					sidebarEl.innerHTML = html;
					if (base) {
						sidebarEl.querySelectorAll("a[href]").forEach(function (a) {
							var href = a.getAttribute("href");
							if (href && !href.startsWith("http") && !href.startsWith("/") && !href.startsWith("#")) {
								a.setAttribute("href", base + href);
							}
						});
					}
					initSidebar(sidebarEl);
				})
		);
	}

	if (headerEl) {
		loads.push(
			fetch(base + "components/header.html")
				.then(res => res.text())
				.then(html => {
					headerEl.innerHTML = html;
					initLogoutButton();
					initProfileModal();
				})
		);
	}

	// 로그인 체크 (로그인 페이지 제외) — 컴포넌트 로딩과 병렬 실행
	if (!isLoginPage) {
		loads.push(
			fetch("/api/auth/current-user")
				.then(function (res) {
					if (!res.ok) throw new Error("not_logged_in");
					return res.json();
				})
				.then(function (user) {
					if (user.position !== "ADMIN") {
						fetch("/api/auth/logout", { method: "POST" });
						sessionStorage.clear();
						window.location.href = "/admin/login.html";
						throw new Error("not_admin");
					}
					// 헤더에 사용자 이름 표시 (DOM 로딩 후)
					window._adminUser = user;
				})
				.catch(function (err) {
					if (err && err.message === "not_admin") return;
					sessionStorage.clear();
					window.location.href = "/admin/login.html";
				})
		);
	}

	Promise.all(loads).then(() => {
		// 헤더 사용자 이름 표시
		if (window._adminUser) {
			var nameEl = document.getElementById("headerUserName");
			if (nameEl) nameEl.textContent = window._adminUser.name || "관리자";
		}

		if (window.feather) {
			feather.replace();
		}
		initSidebarToggle();
		var wrapper = document.querySelector(".wrapper");
		if (wrapper) wrapper.classList.add("ready");
	});
});

function initSidebar(container) {
	const page = document.body.getAttribute("data-page");

	if (page) {
		const activeItem = container.querySelector('.sidebar-item[data-page="' + page + '"]');
		if (activeItem) {
			activeItem.classList.add("active");
		}
	}

	const toggles = container.querySelectorAll(".js-group-toggle");
	toggles.forEach(function (header) {
		var group = header.getAttribute("data-group");
		var items = container.querySelectorAll('.sidebar-item[data-group="' + group + '"]');

		var hasActive = false;
		items.forEach(function (item) {
			if (item.getAttribute("data-page") === page) hasActive = true;
		});

		if (!hasActive) {
			collapseGroup(header, items);
		}

		header.addEventListener("click", function () {
			var isCollapsed = header.classList.contains("collapsed");
			if (isCollapsed) {
				expandGroup(header, items);
			} else {
				collapseGroup(header, items);
			}
		});
	});
}

function initSidebarToggle() {
	var sidebar = document.querySelector(".js-sidebar");
	var toggle = document.querySelector(".js-sidebar-toggle");
	if (sidebar && toggle) {
		toggle.addEventListener("click", function () {
			sidebar.classList.toggle("collapsed");
		});
	}
}

function collapseGroup(header, items) {
	header.classList.add("collapsed");
	var icon = header.querySelector("[data-feather], svg");
	if (icon) icon.style.transform = "rotate(-90deg)";
	items.forEach(function (item) {
		item.style.display = "none";
	});
}

function expandGroup(header, items) {
	header.classList.remove("collapsed");
	var icon = header.querySelector("[data-feather], svg");
	if (icon) icon.style.transform = "rotate(0deg)";
	items.forEach(function (item) {
		item.style.display = "";
	});
}

function initLogoutButton() {
	var logoutBtn = document.getElementById("btnLogout");
	if (logoutBtn) {
		logoutBtn.addEventListener("click", function (e) {
			e.preventDefault();
			fetch("/api/auth/logout", { method: "POST" })
				.finally(function () {
					sessionStorage.clear();
					window.location.href = "/admin/login.html";
				});
		});
	}
}

function initProfileModal() {
	var btn = document.getElementById("btnProfile");
	var modalEl = document.getElementById("profileModal");
	if (!btn || !modalEl) return;

	var msg = document.getElementById("profileMsg");

	btn.addEventListener("click", function (e) {
		e.preventDefault();
		document.getElementById("profileName").value = (window._adminUser && window._adminUser.name) || "";
		document.getElementById("profileCurrentPw").value = "";
		document.getElementById("profileNewPw").value = "";
		document.getElementById("profileNewPw2").value = "";
		msg.textContent = "";
		bootstrap.Modal.getOrCreateInstance(modalEl).show();
	});

	document.getElementById("profileSave").addEventListener("click", function () {
		var name = document.getElementById("profileName").value.trim();
		var currentPassword = document.getElementById("profileCurrentPw").value;
		var newPassword = document.getElementById("profileNewPw").value;
		var newPassword2 = document.getElementById("profileNewPw2").value;

		if (newPassword && newPassword !== newPassword2) {
			msg.textContent = "새 비밀번호 확인이 일치하지 않습니다.";
			return;
		}

		fetch("/api/auth/profile", {
			method: "PUT",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ name: name, currentPassword: currentPassword, newPassword: newPassword })
		}).then(function (res) {
			return res.json().catch(function () { return {}; }).then(function (data) {
				if (!res.ok) {
					msg.textContent = data.message || "저장에 실패했습니다.";
					return;
				}
				var nameEl = document.getElementById("headerUserName");
				if (nameEl) nameEl.textContent = data.name || name || "관리자";
				if (window._adminUser) window._adminUser.name = data.name || name;
				bootstrap.Modal.getOrCreateInstance(modalEl).hide();
				alert("저장되었습니다.");
			});
		});
	});
}