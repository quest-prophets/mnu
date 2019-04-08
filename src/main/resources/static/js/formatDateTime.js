dates = document.getElementsByClassName("datetime-to-format");
for (let i = 0; i < dates.length; ++i) {
    const date = new Date(dates[i].innerText);
    const day = date.getDate().length === 2 ? date.getDate() : "0" + date.getDate();
    const month = (date.getMonth() + 1).length === 2 ? date.getMonth() + 1 : "0" + (date.getMonth() + 1);
    const year = date.getFullYear();
    const hours = ("0" + date.getHours()).slice(-2);
    const minutes = ("0" + date.getMinutes()).slice(-2);
    const seconds = ("0" + date.getSeconds()).slice(-2);
    dates[i].innerText = `${day}.${month}.${year} ${hours}:${minutes}:${seconds}`;
}