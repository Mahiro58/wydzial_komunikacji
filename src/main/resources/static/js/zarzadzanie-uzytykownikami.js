const rola = localStorage.getItem("rola");
let wszyscyUzytkownicy = [];

if (rola !== "ADMIN") {
    alert("Brak dostępu");
    window.location.href = "/index.html";
}

async function pobierzUzytkownikow() {
    const response = await fetch("/uzytkownik");
    wszyscyUzytkownicy = await response.json();

    renderujTabele(wszyscyUzytkownicy);
}

function renderujTabele(users) {
    const tabela = document.getElementById("tabela");
    tabela.innerHTML = "";

    if (!users.length) {
        tabela.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center;">
                    Brak użytkowników do wyświetlenia
                </td>
            </tr>
        `;
        return;
    }

    users.forEach(u => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${u.id}</td>
            <td>${u.imie ?? ""}</td>
            <td>${u.nazwisko ?? ""}</td>
            <td>${u.email ?? ""}</td>
            <td>${u.telefon ?? ""}</td>
            <td>
                <select id="rola-${u.id}">
                    <option value="USER">USER</option>
                    <option value="URZEDNIK">URZEDNIK</option>
                    <option value="ADMIN">ADMIN</option>
                </select>
            </td>
            <td>
                <button class="btn" type="button" onclick="zmienRole(${u.id})">
                    Zapisz rolę
                </button>
            </td>
        `;

        tabela.appendChild(row);
        document.getElementById(`rola-${u.id}`).value = u.rola;
    });
}

function filtrujUzytkownikow() {
    const filtr = document.getElementById("filtr").value.toLowerCase().trim();

    const przefiltrowani = wszyscyUzytkownicy.filter(u => {
        return (
            String(u.id).includes(filtr)
            || (u.imie ?? "").toLowerCase().includes(filtr)
            || (u.nazwisko ?? "").toLowerCase().includes(filtr)
            || (u.email ?? "").toLowerCase().includes(filtr)
            || (u.telefon ?? "").toLowerCase().includes(filtr)
            || (u.rola ?? "").toLowerCase().includes(filtr)
        );
    });

    renderujTabele(przefiltrowani);
}

async function zmienRole(id) {
    const nowaRola = document.getElementById(`rola-${id}`).value;

    const response = await fetch(`/uzytkownik/${id}/rola?rola=${nowaRola}`, {
        method: "PATCH"
    });

    if (response.ok) {
        pokazKomunikat("Rola została zmieniona.", "success");

        await pobierzUzytkownikow();
        filtrujUzytkownikow();
    } else {
        const error = await response.text();
        pokazKomunikat("Błąd zmiany roli: " + error, "error");
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
    pobierzUzytkownikow();
});