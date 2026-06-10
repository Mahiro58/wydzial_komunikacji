document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("loginForm");
    const msg = document.getElementById("msg");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        const data = {
            email: document.getElementById("email").value,
            haslo: document.getElementById("haslo").value
        };

        try {
            const response = await fetch("/auth/login", {
                method: "POST",
                headers: {
                    "Content-Type": "application/json"
                },
                body: JSON.stringify(data)
            });

            if (!response.ok) {
                msg.className = "alert alert-danger";
                msg.innerText = "Błędny email lub hasło";
                return;
            }

            const user = await response.json();

            localStorage.setItem("userId", user.id);
            localStorage.setItem("email", user.email);
            localStorage.setItem("rola", user.rola);

            msg.className = "alert alert-success";
            msg.innerText = user.komunikat;

            setTimeout(() => {
                if (user.rola === "USER") {
                    window.location.href = "/user-panel.html";
                }

                if (user.rola === "URZEDNIK") {
                    window.location.href = "/urzednik-panel.html";
                }

                if (user.rola === "ADMIN") {
                    window.location.href = "/admin-panel.html";
                }
            }, 700);

        } catch (error) {
            msg.className = "alert alert-danger";
            msg.innerText = "Błąd połączenia z serwerem";
        }
    });
});