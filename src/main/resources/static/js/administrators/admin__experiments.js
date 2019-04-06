document.addEventListener('click', (e) => {
    const experimentId = e.target.dataset.expId;
    const action = experimentId && e.target.dataset.action;

    if (action === "accept") {
        fetch("/admin/acceptExperiment/" + experimentId, {method: 'POST'})
            .then(() => {
                alert()
            })
    }
    else if (action === "reject") {
        fetch("/admin/rejectExperiment/" + experimentId, {method: 'POST'})
            .then(() => {
                alert()
            })
    }
});