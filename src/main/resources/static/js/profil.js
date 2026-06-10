const userId = localStorage.getItem("userId");

if (!userId) {
    window.location.href = "/login.html";
}

async function pobierzProfil() {
    const response = await fetch("/uzytkownik/" + userId);
    const user = await response.json();

    document.getElementById("imie").value = user.imie ?? "";
    document.getElementById("nazwisko").value = user.nazwisko ?? "";
    document.getElementById("email").value = user.email ?? "";
    document.getElementById("telefon").value = user.telefon ?? "";
}

async function zapisz(event) {
    event.preventDefault();

    const data = {
        email: document.getElementById("email").value,
        telefon: document.getElementById("telefon").value
    };

    const response = await fetch("/uzytkownik/" + userId + "/kontakt", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });

    if (response.ok) {
        pokazKomunikat("Profil zapisany", "success");
        localStorage.setItem("email", data.email);
    } else {
        pokazKomunikat("Błąd zapisu", "error");
    }
}

function pokazKomunikat(tresc, typ) {
    const msg = document.getElementById("msg");

    if (typ === "success") {
        msg.className = "alert alert-success";
    } else {
        msg.className = "alert alert-error";
    }

    msg.innerText = tresc;
}

document.addEventListener("DOMContentLoaded", () => {
    pobierzProfil();
});