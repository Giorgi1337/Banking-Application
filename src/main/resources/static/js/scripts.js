document.addEventListener('DOMContentLoaded', function() {
    // Search functionality by Account Holder Name
    document.getElementById('searchInput')?.addEventListener('input', function() {
        const query = this.value.toLowerCase();
        const rows = document.querySelectorAll('#accountsTableBody tr');
        rows.forEach(row => {
            const nameCell = row.querySelector('td:nth-child(2)'); // Account Holder Name column
            if (nameCell.textContent.toLowerCase().includes(query)) {
                row.style.display = '';
            } else {
                row.style.display = 'none';
            }
        });
    });

    // Initialize tooltips
    document.querySelectorAll('[data-bs-toggle="tooltip"]').forEach(tooltipEl => {
        new bootstrap.Tooltip(tooltipEl);
    });

    // Update progress bar for Create and Update Account forms
    const form = document.getElementById('createAccountForm') || document.getElementById('updateAccountForm');
    if (form) {
        const inputs = form.querySelectorAll('input[required]');
        const progressBar = document.getElementById('progressBar');

        function updateProgressBar() {
            const filledFields = Array.from(inputs).filter(input => input.value.trim() !== '');
            const progress = (filledFields.length / inputs.length) * 100;
            progressBar.style.width = `${progress}%`;
            progressBar.setAttribute('aria-valuenow', progress);
        }

        inputs.forEach(input => {
            input.addEventListener('input', updateProgressBar);
        });

        // Initialize progress bar on load
        updateProgressBar();
    }
});
