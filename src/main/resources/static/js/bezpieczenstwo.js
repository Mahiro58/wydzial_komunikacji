document.addEventListener("DOMContentLoaded", () => {
    const rola = localStorage.getItem("rola");
    const email = localStorage.getItem("email");

    if (rola !== "ADMIN") {
        alert("Brak dostępu do bezpieczeństwa systemu");
        window.location.href = "/index.html";
        return;
    }

    document.getElementById("email").innerText = email ?? "";
});