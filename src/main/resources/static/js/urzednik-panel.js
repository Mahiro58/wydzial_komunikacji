const rola = localStorage.getItem("rola");

if (rola !== "URZEDNIK" && rola !== "ADMIN") {
    alert("Brak dostępu do panelu urzędnika");
    window.location.href = "/index.html";
}

let wszystkieWnioski = [];

async function pobierzWnioski() {
    const response = await fetch("/wniosek");

    if (!response.ok) {
        pokazKomunikat("Nie udało się pobrać wniosków.", "error");
        return;
    }

    wszystkieWnioski = await response.json();
    renderujTabele(wszystkieWnioski);
}

function renderujTabele(wnioski) {
    const tabela = document.getElementById("tabela");
    tabela.innerHTML = "";

    if (!wnioski.length) {
        tabela.innerHTML = `
            <div class="alert alert-warning">
                Brak wniosków do wyświetlenia
            </div>
        `;
        return;
    }

    wnioski.forEach(w => {
        const card = document.createElement("div");

        const u = w.uzytkownik;
        const p = w.pojazd;
        const dokumentyId = `dokumenty-${w.id}`;
        const czyZatwierdzony = w.status === "ZATWIERDZONY";

        const daneUzytkownika = `
            ${u?.imie ?? ""} ${u?.nazwisko ?? ""}<br>
            Email: ${u?.email ?? "-"}<br>
            Telefon: ${u?.telefon ?? "-"}
        `;

        const danePojazdu = p
            ? `
                <strong>Istniejący pojazd</strong><br>
                Marka: ${p.marka ?? "-"}<br>
                Model: ${p.model ?? "-"}<br>
                VIN: ${p.vin ?? "-"}<br>
                Rok: ${p.rok ?? "-"}<br>
                Nr rej.: ${p.numerRejestracyjny ?? "-"}
            `
            : `
                <strong>Dane z wniosku</strong><br>
                Marka: ${w.marka ?? "-"}<br>
                Model: ${w.model ?? "-"}<br>
                VIN: ${w.vin ?? "-"}<br>
                Rok: ${w.rok ?? "-"}<br>
                Rodzaj: ${w.rodzajPojazdu ?? "-"}<br>
                Przeznaczenie: ${w.przeznaczenie ?? "-"}<br>
                Dotychczasowy nr: ${w.numerRejestracyjny ?? "-"}<br>
                Typ tablic: ${w.typTablic ?? "-"}<br>
                Własny nr: ${w.numerIndywidualny ?? "-"}
            `;

        const platnosc = `
            Kwota: ${w.kwotaOplaty ?? 0} zł<br>
            Status: ${
                w.oplacono
                    ? "<span class='badge badge-success'>ZAPŁACONO</span>"
                    : "<span class='badge badge-danger'>NIEOPŁACONO</span>"
            }
        `;

        card.className = "request-card";

        card.innerHTML = `
            <div class="request-card-header">
                <div>
                    <h3>Wniosek #${w.id}</h3>
                    <p>${w.typ}</p>
                </div>

                <span class="badge">${w.status}</span>
            </div>

            <div class="request-grid">
                <div>
                    <h4>Użytkownik</h4>
                    <p>${daneUzytkownika}</p>
                </div>

                <div>
                    <h4>Dane pojazdu</h4>
                    <p>${danePojazdu}</p>
                </div>

                <div>
                    <h4>Płatność</h4>
                    <p>${platnosc}</p>
                </div>

                <div>
                    <h4>Dokumenty</h4>
                    <p id="${dokumentyId}">Ładowanie...</p>
                </div>
            </div>

            <div class="request-description">
                <h4>Opis</h4>
                <p>${w.opis ?? "-"}</p>
            </div>

            <div class="request-decision">
                <h4>Decyzja</h4>

                <label for="komentarz-${w.id}">Informacja dla użytkownika</label>
                <textarea id="komentarz-${w.id}" rows="4" ${czyZatwierdzony ? "disabled" : ""}>${w.komentarzUrzednika ?? ""}</textarea>

                <label for="status-${w.id}">Status</label>
                <select id="status-${w.id}" ${czyZatwierdzony ? "disabled" : ""}>
                    <option value="ZLOZONY">ZŁOŻONY</option>
                    <option value="W_TRAKCIE">W TRAKCIE</option>
                    <option value="DO_POPRAWY">DO POPRAWY</option>
                    <option value="ZATWIERDZONY">ZATWIERDZONY</option>
                    <option value="ODRZUCONY">ODRZUCONY</option>
                </select>

                <div class="actions">
                    <button class="btn" type="button" onclick="zmienStatus(${w.id})" ${czyZatwierdzony ? "disabled" : ""}>
                        Zapisz status
                    </button>
                </div>

                ${
                    czyZatwierdzony
                        ? "<small>Status zatwierdzony — zablokowany</small>"
                        : ""
                }
            </div>
        `;

        tabela.appendChild(card);
        document.getElementById(`status-${w.id}`).value = w.status;

        pobierzDokumenty(w.id);
    });
}

async function pobierzDokumenty(wniosekId) {
    const cell = document.getElementById(`dokumenty-${wniosekId}`);

    if (!cell) return;

    const response = await fetch(`/dokumenty/wniosek/${wniosekId}`);

    if (!response.ok) {
        cell.innerHTML = "Błąd pobierania dokumentów";
        return;
    }

    const dokumenty = await response.json();

    if (dokumenty.length === 0) {
        cell.innerHTML = "Brak dokumentów";
        return;
    }

    cell.innerHTML = dokumenty.map(d => `
        <strong>${d.typDokumentu ?? "DOKUMENT"}</strong><br>
        ${d.nazwaPliku ?? "plik"}<br>
        <a href="/dokumenty/${d.id}/download" target="_blank">Pobierz</a>
        <br><br>
    `).join("");
}

function filtrujWnioski() {
    const filtr = document.getElementById("filtr").value.toLowerCase().trim();

    const wynik = wszystkieWnioski.filter(w => {
        return (
            String(w.id).includes(filtr)
            || (w.typ ?? "").toLowerCase().includes(filtr)
            || (w.status ?? "").toLowerCase().includes(filtr)
            || (w.uzytkownik?.email ?? "").toLowerCase().includes(filtr)
            || (w.uzytkownik?.imie ?? "").toLowerCase().includes(filtr)
            || (w.uzytkownik?.nazwisko ?? "").toLowerCase().includes(filtr)
            || (w.vin ?? "").toLowerCase().includes(filtr)
            || (w.marka ?? "").toLowerCase().includes(filtr)
            || (w.model ?? "").toLowerCase().includes(filtr)
            || (w.pojazd?.vin ?? "").toLowerCase().includes(filtr)
            || (w.pojazd?.marka ?? "").toLowerCase().includes(filtr)
            || (w.pojazd?.model ?? "").toLowerCase().includes(filtr)
            || (w.komentarzUrzednika ?? "").toLowerCase().includes(filtr)
        );
    });

    renderujTabele(wynik);
}

async function zmienStatus(id) {
    const status = document.getElementById(`status-${id}`).value;
    const komentarz = document.getElementById(`komentarz-${id}`).value;

    const response = await fetch(
        `/wniosek/${id}/status?status=${status}&komentarz=${encodeURIComponent(komentarz)}`,
        {
            method: "PATCH"
        }
    );

    if (response.ok) {
        pokazKomunikat("Status został zmieniony.", "success");

        await pobierzWnioski();
        filtrujWnioski();
    } else {
        const error = await response.text();
        pokazKomunikat("Błąd zmiany statusu: " + error, "error");
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
    pobierzWnioski();
});