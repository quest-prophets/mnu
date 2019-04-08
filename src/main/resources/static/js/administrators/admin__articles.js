document.addEventListener('click', (e) => {
    if (e.target.dataset.action === "reward") {
        e.preventDefault();
        const empId = e.target.dataset.empid;
        let rewardInput = document.getElementById(`reward-input-${empId}`);
        let empIdInput = document.getElementById(`empId-input-${empId}`);
        if (rewardInput.classList.contains("hidden")) {
            rewardInput.classList.remove("hidden");
        } else {
            empIdInput.id = "id_cash";
            empIdInput.name = "id_cash";
            rewardInput.id = "reward";
            rewardInput.name = "reward";
            fetch("/admin/giveReward", {method: 'POST'})
                .then(() => {
                    const form = document.getElementById(`reward-form-${empId}`);
                    form.parentNode.removeChild(form);
                })
        }
    }
});