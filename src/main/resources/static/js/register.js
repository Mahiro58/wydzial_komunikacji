document.addEventListener("DOMContentLoaded", () => {
    const form = document.getElementById("registerForm");

    form.addEventListener("submit", async (e) => {
        e.preventDefault();

        await register();
    });
});

async function register() {
    const msg = document.getElementById("msg");

    const data = {
        imie: document.getElementById("imie").value,
        nazwisko: document.getElementById("nazwisko").value,
        email: document.getElementById("email").value,
        telefon: document.getElementById("telefon").value,
        haslo: document.getElementById("haslo").value
    };

    try {
        const response = await fetch("/auth/register", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(data)
        });

        const text = await response.text();

        if (response.ok) {
            msg.className = "alert alert-success";
            msg.innerText = text;

            setTimeout(() => {
                window.location.href = "/login.html";
            }, 1200);

        } else {
            msg.className = "alert alert-danger";
            msg.innerText = text;
        }

    } catch (error) {
        msg.className = "alert alert-danger";
        msg.innerText = "Błąd połączenia z serwerem";
    }
}