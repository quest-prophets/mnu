document.addEventListener('click', (e) => {
    const requestId = e.target.dataset.requestId;
    const action = requestId && e.target.dataset.action;

    if (action === "accept") {
        fetch(`/man/acceptNewEquipment/` + requestId, {method: 'POST'})
            .then(() => {
                dealWithWeapon(true, requestId);
            })
    } else if (action === "reject") {
        fetch(`/man/rejectNewEquipment/` + requestId, {method: 'POST'})
            .then(() => {
                dealWithWeapon(false, requestId);
            })
    }
});

function dealWithWeapon(isAccept, requestId) {
    document.getElementById("request-buttons-" + requestId).classList.add("hidden");
    document.getElementById("request-flash-" + requestId).classList.remove('hidden');
    if (isAccept) {
        document.getElementById("request-content-" + requestId).classList.add('approved');
        document.getElementById("request-content-" + requestId).classList.remove('finished');
        document.getElementById("request-content-" + requestId).classList.remove('rejected');
        document.getElementById("request-flash-" + requestId).innerHTML = "Accepted";
    } else {
        document.getElementById("request-content-" + requestId).classList.add('rejected');
        document.getElementById("request-content-" + requestId).classList.remove('finished');
        document.getElementById("request-content-" + requestId).classList.remove('approved');
        document.getElementById("request-flash-" + requestId).innerHTML = "Rejected";
    }
}