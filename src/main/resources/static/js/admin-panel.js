document.addEventListener("DOMContentLoaded", () => {

    const rola = localStorage.getItem("rola");
    const email = localStorage.getItem("email");

    if (rola !== "ADMIN") {
        alert("Brak dostępu do panelu administratora");
        window.location.href = "/index.html";
        return;
    }

    const emailElement = document.getElementById("email");

    if (emailElement) {
        emailElement.innerText = email ?? "";
    }

});