document.addEventListener("DOMContentLoaded", () => {
    const hamburgerBtn = document.getElementById("hamburgerBtn");
    const navLinks = document.getElementById("navLinks");
    const logoutBtn = document.getElementById("logoutBtn");
    const userInfo = document.getElementById("userInfo");

    const userId = localStorage.getItem("userId");
    const email = localStorage.getItem("email");
    const rola = localStorage.getItem("rola") || localStorage.getItem("role");

    const isLoggedIn = !!userId || !!email;

    if (hamburgerBtn && navLinks) {
        hamburgerBtn.addEventListener("click", () => {
            navLinks.classList.toggle("active");
        });
    }

    document.querySelectorAll("[data-auth='true']").forEach(element => {
        element.classList.toggle("hidden", !isLoggedIn);
    });

    document.querySelectorAll("[data-guest='true']").forEach(element => {
        element.classList.toggle("hidden", isLoggedIn);
    });

    document.querySelectorAll("[data-role]").forEach(element => {
        const requiredRole = element.getAttribute("data-role");

        if (!isLoggedIn || rola !== requiredRole) {
            element.classList.add("hidden");
        }
    });

    if (userInfo && isLoggedIn) {
        userInfo.innerHTML = `
            <strong>${email || "Użytkownik"}</strong>
            <br>
            <small>${rola || "BRAK ROLI"}</small>
        `;
    }

    if (logoutBtn) {
        logoutBtn.addEventListener("click", () => {
            localStorage.clear();
            window.location.href = "/login.html";
        });
    }
});