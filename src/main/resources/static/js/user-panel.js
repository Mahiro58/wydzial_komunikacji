const email = localStorage.getItem("email");
const rola = localStorage.getItem("rola");

if (!email) {
    window.location.href = "/login.html";
}

document.addEventListener("DOMContentLoaded", () => {
    document.getElementById("email").innerText = email;
    document.getElementById("rola").innerText = rola;
});