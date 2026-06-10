const userId = localStorage.getItem("userId");

if (!userId) {
    window.location.href = "/login.html";
}

async function zlozWniosek(event) {
    event.preventDefault();

    const typ = document.getElementById("typ").value;

    if (!typ) {
        pokazKomunikat("Wybierz typ wniosku.", "error");
        return;
    }

    if (!document.getElementById("oplacono").checked) {
        pokazKomunikat("Aby wysłać wniosek, musisz oznaczyć opłatę jako opłaconą.", "error");
        return;
    }

    if (typ === "REJESTRACJA" || typ === "CZASOWA") {
        const vin = document.getElementById("vin").value.trim();
        const marka = document.getElementById("marka").value.trim();
        const model = document.getElementById("model").value.trim();
        const rok = document.getElementById("rok").value;
        const typTablic = document.getElementById("typTablic").value;
        const zachowajNumer = document.getElementById("zachowajNumer").checked;
        const numerRejestracyjny = document.getElementById("numerRejestracyjny").value.trim();
        const numerIndywidualny = document.getElementById("numerIndywidualny").value.trim();

        if (vin.length !== 17) {
            pokazKomunikat("VIN musi mieć dokładnie 17 znaków.", "error");
            return;
        }

        if (!marka) {
            pokazKomunikat("Podaj markę pojazdu.", "error");
            return;
        }

        if (!model) {
            pokazKomunikat("Podaj model pojazdu.", "error");
            return;
        }

        if (!rok || Number(rok) < 1900 || Number(rok) > new Date().getFullYear() + 1) {
            pokazKomunikat("Podaj poprawny rok produkcji.", "error");
            return;
        }

        if (zachowajNumer && !numerRejestracyjny) {
            pokazKomunikat("Zaznaczono zachowanie numeru, ale nie podano dotychczasowego numeru.", "error");
            return;
        }

        if (typTablic === "INDYWIDUALNE" && !numerIndywidualny) {
            pokazKomunikat("Dla tablic indywidualnych wpisz własny numer.", "error");
            return;
        }
    }

    const pojazdSelect = document.getElementById("pojazdId");
    const pojazdIdValue = pojazdSelect ? pojazdSelect.value : "";

    if ((typ === "WYREJESTROWANIE" || typ === "ZBYCIE") && !pojazdIdValue) {
        pokazKomunikat("Wybierz pojazd.", "error");
        return;
    }

    const data = {
        uzytkownikId: Number(userId),
        pojazdId: pojazdIdValue ? Number(pojazdIdValue) : null,
        typ: typ,
        opis: document.getElementById("opis").value,
        urzednikId: null,
        vin: document.getElementById("vin")?.value || null,
        marka: document.getElementById("marka")?.value || null,
        model: document.getElementById("model")?.value || null,
        rok: document.getElementById("rok")?.value ? Number(document.getElementById("rok").value) : null,
        numerRejestracyjny: document.getElementById("numerRejestracyjny")?.value || null,
        rodzajPojazdu: document.getElementById("rodzajPojazdu")?.value || null,
        przeznaczenie: document.getElementById("przeznaczenie")?.value || null,
        dataNabycia: document.getElementById("dataNabycia")?.value || null,
        typTablic: document.getElementById("typTablic")?.value || null,
        zachowajNumer: document.getElementById("zachowajNumer")?.checked || false,
        numerIndywidualny: document.getElementById("numerIndywidualny")?.value || null,
        powodWyrejestrowania: document.getElementById("powodWyrejestrowania")?.value || null,
        kwotaOplaty: parseInt(document.getElementById("oplata").innerText.replace(" zł", "")) || 0,
        oplacono: document.getElementById("oplacono").checked
    };

    console.log("Wysyłam wniosek:", data);

    const response = await fetch("/wniosek", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(data)
    });

    if (response.ok) {
        const saved = await response.json();

        await wyslijDokumenty(saved.id);

        pokazKomunikat("Wniosek złożony. ID: " + saved.id + ", status: " + saved.status, "success");
    } else {
        const error = await response.text();
        pokazKomunikat("Błąd składania wniosku: " + error, "error");
    }
}

async function pobierzPojazdy() {
    const response = await fetch("/pojazd/uzytkownik/" + userId);
    const pojazdy = await response.json();
    const select = document.getElementById("pojazdId");

    select.innerHTML = `<option value="">-- wybierz pojazd --</option>`;

    pojazdy.forEach(p => {
        const option = document.createElement("option");

        option.value = p.id;
        option.text = `${p.marka} ${p.model} (${p.numerRejestracyjny ?? "brak nr"})`;

        select.appendChild(option);
    });
}

async function wyslijDokumenty(wniosekId) {
    const formData = new FormData();

    dodajPlik(formData, "plikDowodWlasnosci", "DOWOD_WLASNOSCI");
    dodajPlik(formData, "plikDowodRejestracyjny", "DOWOD_REJESTRACYJNY");
    dodajPlik(formData, "plikKartaPojazdu", "KARTA_POJAZDU");
    dodajPlik(formData, "plikPolisaOc", "POLISA_OC");
    dodajPlik(formData, "plikPotwierdzenieOplat", "POTWIERDZENIE_OPLAT");

    if (!formData.has("files")) {
        console.log("Brak dokumentów do wysłania");
        return;
    }

    const response = await fetch("/dokumenty/wniosek/" + wniosekId, {
        method: "POST",
        body: formData
    });

    if (!response.ok) {
        const error = await response.text();
        pokazKomunikat("Wniosek zapisany, ale dokumenty nie zostały wysłane: " + error, "error");
    }
}

function dodajPlik(formData, inputId, typDokumentu) {
    const input = document.getElementById(inputId);

    if (input.files.length > 0) {
        formData.append("files", input.files[0]);
        formData.append("typy", typDokumentu);
    }
}

function obliczOplate() {
    const typ = document.getElementById("typ").value;
    const typTablic = document.getElementById("typTablic").value;
    const dodatkowaTablica = document.getElementById("dodatkowaTablica").checked;
    const zachowajNumer = document.getElementById("zachowajNumer").checked;

    let oplata = 0;

    if (typ === "REJESTRACJA") oplata = 160;
    if (typ === "CZASOWA") oplata = 80;
    if (typ === "WYREJESTROWANIE") oplata = 30;
    if (typ === "ZBYCIE") oplata = 0;

    if (typ === "REJESTRACJA" || typ === "CZASOWA") {
        if (typTablic === "INDYWIDUALNE") oplata += 1000;
        if (typTablic === "ZMNIEJSZONE") oplata += 20;
        if (dodatkowaTablica) oplata += 50;
        if (zachowajNumer) oplata -= 40;
    }

    if (oplata < 0) oplata = 0;

    document.getElementById("oplata").innerText = oplata + " zł";
}

function pokazPolaDlaTypu() {
    const typ = document.getElementById("typ").value;

    document.getElementById("sekcjaDanePojazdu").style.display = "none";
    document.getElementById("sekcjaWyrejestrowanie").style.display = "none";
    document.getElementById("sekcjaZbycie").style.display = "none";
    document.getElementById("sekcjaWyborPojazdu").style.display = "none";
    document.getElementById("sekcjaTablice").style.display = "none";

    if (typ === "REJESTRACJA" || typ === "CZASOWA") {
        document.getElementById("sekcjaDanePojazdu").style.display = "block";
        document.getElementById("sekcjaTablice").style.display = "block";
    }

    if (typ === "WYREJESTROWANIE") {
        document.getElementById("sekcjaWyborPojazdu").style.display = "block";
        document.getElementById("sekcjaWyrejestrowanie").style.display = "block";
    }

    if (typ === "ZBYCIE") {
        document.getElementById("sekcjaWyborPojazdu").style.display = "block";
        document.getElementById("sekcjaZbycie").style.display = "block";
    }

    pokazNumerIndywidualny();
    obliczOplate();
}

function pokazNumerIndywidualny() {
    const typ = document.getElementById("typ").value;
    const typTablic = document.getElementById("typTablic").value;

    document.getElementById("sekcjaNumerIndywidualny").style.display =
        (typ === "REJESTRACJA" || typ === "CZASOWA") && typTablic === "INDYWIDUALNE"
            ? "block"
            : "none";
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
    pobierzPojazdy();
    obliczOplate();
    pokazPolaDlaTypu();
    pokazNumerIndywidualny();
});