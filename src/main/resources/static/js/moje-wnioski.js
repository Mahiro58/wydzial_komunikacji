const userId = localStorage.getItem("userId");

document.addEventListener("DOMContentLoaded", () => {
    if (!userId) {
        window.location.href = "/login.html";
        return;
    }

    pobierzWnioski();
    polaczWebSocket();
});

async function pobierzWnioski() {
    const response = await fetch("/wniosek/uzytkownik/" + userId);

    if (!response.ok) {
        alert("Nie udało się pobrać wniosków");
        return;
    }

    const wnioski = await response.json();
    const tabela = document.getElementById("tabela");

    tabela.innerHTML = "";

    if (!wnioski.length) {
        tabela.innerHTML = `
            <tr>
                <td colspan="7" style="text-align:center;">
                    Brak złożonych wniosków
                </td>
            </tr>
        `;
        return;
    }

    wnioski.forEach(w => {
        const row = document.createElement("tr");

        row.innerHTML = `
            <td>${w.id}</td>
            <td>${w.typ}</td>
            <td>
                ${w.status}
                ${
                    w.status === "DO_POPRAWY"
                    ? "<br><span class='badge badge-danger'>Wymaga poprawy</span>"
                    : ""
                }
                ${
                    w.status === "ZATWIERDZONY"
                    ? "<br><span class='badge badge-success'>Zatwierdzony</span>"
                    : ""
                }
            </td>
            <td>${w.dataZlozenia ?? "-"}</td>
            <td>${w.opis ?? ""}</td>
            <td>${w.komentarzUrzednika ? w.komentarzUrzednika : "-"}</td>
            <td>
                ${
                    w.status === "DO_POPRAWY"
                    ? `<button class="btn btn-secondary" type="button" onclick="przejdzDoPoprawy(${w.id})">Popraw</button>`
                    : "-"
                }
            </td>
        `;

        tabela.appendChild(row);
    });
}

function przejdzDoPoprawy(id) {
    window.location.href = "/popraw-wniosek.html?id=" + id;
}

function polaczWebSocket() {
    const socket = new SockJS("/my-websocket");
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, function () {
        console.log("Połączono z WebSocket");

        stompClient.subscribe("/topic/wniosek/events", function (message) {
            const event = JSON.parse(message.body);

            const powiadomienie = document.getElementById("powiadomienie");

            powiadomienie.className = "alert alert-success";
            powiadomienie.innerText =
                "🔔 Zmieniono status wniosku ID " +
                event.wniosekId +
                " z " +
                event.oldStatus +
                " na " +
                event.newStatus;

            pobierzWnioski();
        });
    });
}