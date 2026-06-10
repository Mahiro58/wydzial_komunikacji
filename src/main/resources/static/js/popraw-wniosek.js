const userId = localStorage.getItem("userId");

if (!userId) {
    window.location.href = "/login.html";
}

const params = new URLSearchParams(window.location.search);
const wniosekId = params.get("id");

let aktualnyWniosek = null;

async function pobierzWniosek() {
    const response = await fetch("/wniosek/uzytkownik/" + userId);

    if (!response.ok) {
        pokazKomunikat("Nie udało się pobrać wniosku.", "error");
        return;
    }

    const wnioski = await response.json();

    aktualnyWniosek = wnioski.find(w => String(w.id) === String(wniosekId));

    if (!aktualnyWniosek) {
        pokazKomunikat("Nie znaleziono wniosku.", "error");
        return;
    }

    if (aktualnyWniosek.status !== "DO_POPRAWY") {
        pokazKomunikat("Ten wniosek nie jest oznaczony jako DO POPRAWY.", "error");
        return;
    }

    document.getElementById("info").innerText =
        "Poprawiasz wniosek ID: " + aktualnyWniosek.id + ", typ: " + aktualnyWniosek.typ;

    if (aktualnyWniosek.komentarzUrzednika) {
        document.getElementById("komentarzUrzednikaBox").style.display = "block";
        document.getElementById("komentarzUrzednika").innerText = aktualnyWniosek.komentarzUrzednika;
    }

    document.getElementById("opis").value = aktualnyWniosek.opis ?? "";
    document.getElementById("rodzajPojazdu").value = aktualnyWniosek.rodzajPojazdu ?? "";
    document.getElementById("przeznaczenie").value = aktualnyWniosek.przeznaczenie ?? "";
    document.getElementById("marka").value = aktualnyWniosek.marka ?? "";
    document.getElementById("model").value = aktualnyWniosek.model ?? "";
    document.getElementById("rok").value = aktualnyWniosek.rok ?? "";
    document.getElementById("vin").value = aktualnyWniosek.vin ?? "";
    document.getElementById("numerRejestracyjny").value = aktualnyWniosek.numerRejestracyjny ?? "";
    document.getElementById("dataNabycia").value = aktualnyWniosek.dataNabycia ?? "";
    document.getElementById("typTablic").value = aktualnyWniosek.typTablic ?? "ZWYCZAJNE";
    document.getElementById("zachowajNumer").checked = aktualnyWniosek.zachowajNumer === true;
    document.getElementById("numerIndywidualny").value = aktualnyWniosek.numerIndywidualny ?? "";
}

async function zapiszPoprawke() {
    if (!aktualnyWniosek) {
        return;
    }

    const data = {
        typ: aktualnyWniosek.typ,
        uzytkownikId: Number(userId),
        pojazdId: aktualnyWniosek.pojazd?.id ?? null,
        opis: document.getElementById("opis").value,
        urzednikId: null,
        vin: document.getElementById("vin").value || null,
        marka: document.getElementById("marka").value || null,
        model: document.getElementById("model").value || null,
        rok: document.getElementById("rok").value ? Number(document.getElementById("rok").value) : null,
        numerRejestracyjny: document.getElementById("numerRejestracyjny").value || null,
        rodzajPojazdu: document.getElementById("rodzajPojazdu").value || null,
        przeznaczenie: document.getElementById("przeznaczenie").value || null,
        dataNabycia: document.getElementById("dataNabycia").value || null,
        typTablic: document.getElementById("typTablic").value || null,
        zachowajNumer: document.getElementById("zachowajNumer").checked,
        numerIndywidualny: document.getElementById("numerIndywidualny").value || null
    };

    const response = await fetch("/wniosek/" + wniosekId + "/popraw", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });

    if (response.ok) {
        await wyslijDokumenty();

        pokazKomunikat("Wniosek poprawiony i ponownie wysłany do urzędu.", "success");

        setTimeout(() => {
            window.location.href = "/moje-wnioski.html";
        }, 1000);
    } else {
        const error = await response.text();
        pokazKomunikat("Błąd poprawy wniosku: " + error, "error");
    }
}

async function wyslijDokumenty() {
    const formData = new FormData();

    dodajPlik(formData, "plikDowodWlasnosci", "DOWOD_WLASNOSCI");
    dodajPlik(formData, "plikDowodRejestracyjny", "DOWOD_REJESTRACYJNY");
    dodajPlik(formData, "plikKartaPojazdu", "KARTA_POJAZDU");
    dodajPlik(formData, "plikPolisaOc", "POLISA_OC");
    dodajPlik(formData, "plikPotwierdzenieOplat", "POTWIERDZENIE_OPLAT");

    if (!formData.has("files")) {
        return;
    }

    const response = await fetch("/dokumenty/wniosek/" + wniosekId, {
        method: "POST",
        body: formData
    });

    if (!response.ok) {
        const error = await response.text();
        pokazKomunikat("Wniosek poprawiony, ale dokumenty nie zostały wysłane: " + error, "error");
    }
}

function dodajPlik(formData, inputId, typDokumentu) {
    const input = document.getElementById(inputId);

    if (input && input.files && input.files.length > 0) {
        formData.append("files", input.files[0]);
        formData.append("typy", typDokumentu);
    }
}

function pokazKomunikat(tresc, typ) {
    const msg = document.getElementById("msg");

    msg.classList.remove("hidden", "alert-success", "alert-error", "alert-danger");

    if (typ === "success") {
        msg.className = "alert alert-success";
    } else {
        msg.className = "alert alert-error";
    }

    msg.innerText = tresc;
}

document.addEventListener("DOMContentLoaded", () => {
    pobierzWniosek();
});