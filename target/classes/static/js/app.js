document.addEventListener("DOMContentLoaded", () => {
    let popupHideTimer;

    window.showToast = (message, type = "success") => {
        const toastEl = document.getElementById("appToast");
        const toastBody = document.getElementById("toastMessage");

        if (!toastEl || !toastBody || !window.bootstrap || !bootstrap.Toast) {
            return;
        }

        toastBody.innerText = message;
        toastEl.className = `toast align-items-center text-bg-${type} border-0`;

        const toast = bootstrap.Toast.getOrCreateInstance(toastEl, {
            autohide: true,
            delay: 4000
        });
        toast.show();
    };

    window.showNotification = (message, title = "New Notification") => {
        const popup = document.getElementById("popupNotification");
        const popupTitle = document.getElementById("notificationPopupTitle");
        const popupText = document.getElementById("notificationText");

        if (!popup || !popupTitle || !popupText) {
            return;
        }

        popupTitle.innerText = title;
        popupText.innerText = message;
        popup.classList.add("is-visible");

        window.clearTimeout(popupHideTimer);
        popupHideTimer = window.setTimeout(() => {
            popup.classList.remove("is-visible");
        }, 4000);
    };

    const modeMarker = document.querySelector("[data-page-mode]");
    if (modeMarker && modeMarker.dataset.pageMode) {
        document.body.classList.add(`app-mode-${modeMarker.dataset.pageMode}`);
    }

    document.querySelectorAll("[data-password-toggle]").forEach((button) => {
        button.addEventListener("click", () => {
            const field = button.closest(".password-field");
            const input = field ? field.querySelector("input") : null;
            const icon = button.querySelector(".fa");
            if (!input) {
                return;
            }

            const showPassword = input.type === "password";
            input.type = showPassword ? "text" : "password";
            button.setAttribute("aria-pressed", String(showPassword));
            button.setAttribute("aria-label", showPassword ? "Hide password" : "Show password");
            field.classList.toggle("password-field--visible", showPassword);

            if (icon) {
                icon.classList.toggle("fa-eye", !showPassword);
                icon.classList.toggle("fa-eye-slash", showPassword);
            }
        });
    });

    const userSearch = document.querySelector("[data-user-search]");
    if (userSearch) {
        const directories = Array.from(document.querySelectorAll("[data-user-directory]"));
        const applyUserFilter = () => {
            const term = userSearch.value.trim().toLowerCase();
            directories.forEach((directory) => {
                const rows = Array.from(directory.querySelectorAll("[data-user-row]"));
                const emptyState = directory.querySelector("[data-user-empty]");
                let visibleRows = 0;

                rows.forEach((row) => {
                    const haystack = (row.dataset.userFilter || "").toLowerCase();
                    const matches = term === "" || haystack.includes(term);
                    row.classList.toggle("d-none", !matches);
                    if (matches) {
                        visibleRows++;
                    }
                });

                if (emptyState) {
                    emptyState.classList.toggle("d-none", visibleRows > 0);
                }
            });
        };

        userSearch.addEventListener("input", applyUserFilter);
        applyUserFilter();
    }

    const header = document.querySelector(".site-header");
    const syncHeader = () => {
        if (!header) {
            return;
        }
        header.classList.toggle("is-scrolled", window.scrollY > 12);
    };

    syncHeader();
    window.addEventListener("scroll", syncHeader, { passive: true });

    const toastPayload = document.querySelector("[data-toast-message]");
    if (toastPayload) {
        window.showToast(toastPayload.innerText, toastPayload.dataset.toastType || "success");
    }

    const notificationToggle = document.querySelector("[data-notification-toggle]");
    const notificationSidebar = document.getElementById("notificationSidebar");
    const notificationBackdrop = document.getElementById("notificationBackdrop");
    const notificationCount = document.getElementById("notificationCount");
    const notificationCloseButtons = Array.from(document.querySelectorAll("[data-notification-close]"));
    const notificationItems = Array.from(document.querySelectorAll("[data-notification-id]"));
    const notificationStorageKey = "rentify_seen_notifications";

    const readSeenNotifications = () => {
        try {
            const stored = window.localStorage.getItem(notificationStorageKey);
            const parsed = stored ? JSON.parse(stored) : [];
            return new Set(Array.isArray(parsed) ? parsed : []);
        } catch (error) {
            return new Set();
        }
    };

    const writeSeenNotifications = (seenNotifications) => {
        try {
            const values = Array.from(seenNotifications).slice(-100);
            window.localStorage.setItem(notificationStorageKey, JSON.stringify(values));
        } catch (error) {
        }
    };

    const seenNotifications = readSeenNotifications();

    const updateNotificationCount = () => {
        if (!notificationCount) {
            return;
        }

        const unseenCount = notificationItems.filter((item) => {
            const notificationId = item.dataset.notificationId;
            return notificationId && !seenNotifications.has(notificationId);
        }).length;

        notificationCount.innerText = String(unseenCount);
    };

    const markNotificationsSeen = (notificationIds) => {
        notificationIds
            .filter(Boolean)
            .forEach((notificationId) => seenNotifications.add(notificationId));
        writeSeenNotifications(seenNotifications);
        updateNotificationCount();
    };

    const openNotificationSidebar = () => {
        if (!notificationSidebar) {
            return;
        }

        notificationSidebar.classList.add("open");
        notificationSidebar.setAttribute("aria-hidden", "false");
        document.body.classList.add("notification-sidebar-open");
        if (notificationBackdrop) {
            notificationBackdrop.classList.add("is-visible");
        }
        if (notificationToggle) {
            notificationToggle.setAttribute("aria-expanded", "true");
        }

        markNotificationsSeen(notificationItems.map((item) => item.dataset.notificationId));
    };

    const closeNotificationSidebar = () => {
        if (!notificationSidebar) {
            return;
        }

        notificationSidebar.classList.remove("open");
        notificationSidebar.setAttribute("aria-hidden", "true");
        document.body.classList.remove("notification-sidebar-open");
        if (notificationBackdrop) {
            notificationBackdrop.classList.remove("is-visible");
        }
        if (notificationToggle) {
            notificationToggle.setAttribute("aria-expanded", "false");
        }
    };

    if (notificationToggle && notificationSidebar) {
        notificationToggle.addEventListener("click", () => {
            if (notificationSidebar.classList.contains("open")) {
                closeNotificationSidebar();
                return;
            }

            openNotificationSidebar();
        });
    }

    notificationCloseButtons.forEach((button) => {
        button.addEventListener("click", closeNotificationSidebar);
    });

    notificationItems.forEach((item) => {
        item.addEventListener("click", () => {
            const notificationId = item.dataset.notificationId;
            if (notificationId) {
                markNotificationsSeen([notificationId]);
            }
        });
    });

    document.addEventListener("keydown", (event) => {
        if (event.key === "Escape") {
            closeNotificationSidebar();
        }
    });

    document.querySelectorAll(".modal").forEach((modalElement) => {
        modalElement.addEventListener("show.bs.modal", () => {
            closeNotificationSidebar();
            const popupNotification = document.getElementById("popupNotification");
            if (popupNotification) {
                popupNotification.classList.remove("is-visible");
            }
        });

        modalElement.addEventListener("shown.bs.modal", () => {
            const focusTarget = modalElement.querySelector("[data-modal-focus], textarea, input, select");
            if (focusTarget instanceof HTMLElement) {
                focusTarget.focus();
            }
        });
    });

    if (notificationItems.length > 0) {
        const newestUnseenNotification = [...notificationItems]
            .filter((item) => !seenNotifications.has(item.dataset.notificationId || ""))
            .sort((left, right) => {
                const leftTime = new Date(left.dataset.notificationCreatedAt || 0).getTime();
                const rightTime = new Date(right.dataset.notificationCreatedAt || 0).getTime();
                return rightTime - leftTime;
            })[0];

        if (newestUnseenNotification) {
            window.showNotification(
                newestUnseenNotification.dataset.notificationMessage || "",
                newestUnseenNotification.dataset.notificationTitle || "New Notification"
            );
            markNotificationsSeen([newestUnseenNotification.dataset.notificationId]);
        } else {
            updateNotificationCount();
        }
    } else {
        updateNotificationCount();
    }

    const skeletonShells = Array.from(document.querySelectorAll("[data-skeleton-shell]"));
    const revealSkeletonContent = () => {
        skeletonShells.forEach((shell) => {
            const loader = shell.querySelector("[data-skeleton-loader]");
            const content = shell.querySelector("[data-skeleton-content]");
            if (!loader || !content) {
                return;
            }

            loader.hidden = true;
            content.hidden = false;
            content.classList.add("skeleton-content--ready");
            content.querySelectorAll(".reveal").forEach((element) => element.classList.add("is-visible"));
        });
    };

    if (skeletonShells.length > 0) {
        skeletonShells.forEach((shell) => {
            const loader = shell.querySelector("[data-skeleton-loader]");
            const content = shell.querySelector("[data-skeleton-content]");
            if (!loader || !content) {
                return;
            }

            loader.hidden = false;
            content.hidden = true;
        });

        const scheduleReveal = () => window.setTimeout(revealSkeletonContent, 600);
        if (document.readyState === "complete") {
            scheduleReveal();
        } else {
            window.addEventListener("load", scheduleReveal, { once: true });
        }
    }

    const revealTargets = Array.from(document.querySelectorAll(
        ".hero-panel, .surface-card, .glass-card, .listing-card, .metric-card, .empty-state, .error-panel, .feature-card, .auth-card, .notification-panel, .results-toolbar"
    ));

    revealTargets.forEach((element, index) => {
        element.classList.add("reveal");
        element.style.setProperty("--reveal-delay", `${Math.min(index * 60, 360)}ms`);
    });

    if (!("IntersectionObserver" in window)) {
        revealTargets.forEach((element) => element.classList.add("is-visible"));
        return;
    }

    const observer = new IntersectionObserver((entries, currentObserver) => {
        entries.forEach((entry) => {
            if (!entry.isIntersecting) {
                return;
            }

            entry.target.classList.add("is-visible");
            currentObserver.unobserve(entry.target);
        });
    }, {
        threshold: 0.12,
        rootMargin: "0px 0px -40px 0px"
    });

    revealTargets.forEach((element) => observer.observe(element));
});
