document.addEventListener("DOMContentLoaded", () => {
    const userId = localStorage.getItem("userId");

    if (!userId) {
        window.location.href = "/login.html";
        return;
    }

    pobierzPojazdy();
});

async function pobierzPojazdy() {
    try {
        const userId = localStorage.getItem("userId");

        const response = await fetch(
            "/pojazd/uzytkownik/" + userId
        );

        const pojazdy = await response.json();

        const tabela =
            document.getElementById("tabela");

        tabela.innerHTML = "";

        if (!pojazdy.length) {
            tabela.innerHTML = `
                <tr>
                    <td colspan="6" style="text-align:center;">
                        Brak przypisanych pojazdów
                    </td>
                </tr>
            `;
            return;
        }

        pojazdy.forEach(p => {
            const row =
                document.createElement("tr");

            row.innerHTML = `
                <td>${p.id}</td>
                <td>${p.marka}</td>
                <td>${p.model}</td>
                <td>${p.rok ?? "-"}</td>
                <td>${p.vin}</td>
                <td>${p.numerRejestracyjny ?? "-"}</td>
            `;

            tabela.appendChild(row);
        });

    } catch (error) {
        console.error(error);
    }
}