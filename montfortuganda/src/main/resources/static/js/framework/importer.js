/* global apiMultipart, apiGet, showErrorMessage, showSuccessMessage */

const AppImporter = (function() {
    let modalEl, titleEl, descEl, fileInput, uploadBtn, cancelBtn;
    let progressContainer, statusText, progressBar;
    let resultContainer, resultMsg, downloadErrorsBtn;
    
    let currentPollingInterval = null;

    function init() {
        modalEl = document.getElementById('erp-import-modal');
        if (!modalEl) {
            console.error("Import modal HTML not found in the DOM.");
            return;
        }

        modalEl = document.getElementById('erp-import-modal');
        titleEl = document.getElementById('import-title');
        descEl = document.getElementById('import-desc');
        fileInput = document.getElementById('import-file-input');
        
        progressContainer = document.getElementById('import-progress-container');
        statusText = document.getElementById('import-status-text');
        progressBar = document.getElementById('import-progress-bar');
        
        resultContainer = document.getElementById('import-result-container');
        resultMsg = document.getElementById('import-result-msg');
        downloadErrorsBtn = document.getElementById('import-download-errors');
        
        cancelBtn = document.getElementById('import-btn-cancel');
        uploadBtn = document.getElementById('import-btn-upload');

        cancelBtn.addEventListener('click', hide);
    }

    function resetUI() {
        fileInput.value = '';
        fileInput.classList.remove('hidden');
        progressContainer.classList.add('hidden');
        resultContainer.classList.add('hidden');
        downloadErrorsBtn.classList.add('hidden');
        uploadBtn.disabled = false;
        uploadBtn.classList.remove('hidden');
        progressBar.value = 0;
        
        if (currentPollingInterval) {
            clearInterval(currentPollingInterval);
            currentPollingInterval = null;
        }
    }

    function hide() {
        if (modalEl) modalEl.classList.add('hidden');
        resetUI();
    }

    function pollProgress(jobId, onSuccess) {
        currentPollingInterval = setInterval(async () => {
            try {
                // Use global apiGet
                const res = await apiGet(`/import/progress/${jobId}`);
                if (!res) return;

                // Handle wrapped response from handleResponSse in api.js if applicable
                const data = res.data ? res.data : res;

                statusText.textContent = `Status: ${data.status}`;
                
                if (data.status === 'COMPLETED' || data.status === 'COMPLETED_WITH_ERRORS' || data.status === 'FAILED') {
                    clearInterval(currentPollingInterval);
                    currentPollingInterval = null;
                    
                    progressContainer.classList.add('hidden');
                    resultContainer.classList.remove('hidden');
                    
                    if (data.status === 'COMPLETED') {
                        resultMsg.textContent = 'Successfully imported rows.';
                        resultMsg.className = 'import-success-text';
                        if (onSuccess) onSuccess();
                    } else {
                        resultMsg.textContent = 'Finished with errors. Check the error report.';
                        resultMsg.className = 'import-error-text';
                        
                        downloadErrorsBtn.onclick = async function(e) {
                            e.preventDefault();
                            try {
                                const response = await fetch(`/api/import/errors/${jobId}`, {
                                    method: 'GET',
                                    headers: getAuthHeaders() // From global.js
                                });
                                if (!response.ok) throw new Error("Failed to download error report");
                                
                                const blob = await response.blob();
                                const url = window.URL.createObjectURL(blob);
                                const a = document.createElement('a');
                                a.href = url;
                                a.download = `Error_Report_${jobId}.xlsx`;
                                document.body.appendChild(a);
                                a.click();
                                window.URL.revokeObjectURL(url);
                                document.body.removeChild(a);
                            } catch(err) {
                                showErrorMessage("Could not download error report.");
                            }
                        };
                        downloadErrorsBtn.classList.remove('hidden');
                        if (data.status === 'COMPLETED_WITH_ERRORS' && onSuccess) onSuccess();
                    }
                    
                    uploadBtn.classList.add('hidden');
                    cancelBtn.textContent = 'Close';
                }
                
            } catch (err) {
                console.error("Polling error", err);
            }
        }, 2000);
    }

    if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
    else init();

    return {
        open: function(moduleName, title = 'Import Data', description = 'Upload an Excel file.', onSuccess = null) {
            if (!modalEl) init();
            resetUI();
            
            titleEl.textContent = title;
            descEl.textContent = description;
            cancelBtn.textContent = 'Cancel';
            
            // Remove old listeners
            const newUploadBtn = uploadBtn.cloneNode(true);
            uploadBtn.parentNode.replaceChild(newUploadBtn, uploadBtn);
            uploadBtn = newUploadBtn;
            
            uploadBtn.addEventListener('click', async () => {
                const file = fileInput.files[0];
                if (!file) {
                    showErrorMessage("Please select a file.");
                    return;
                }
                
                uploadBtn.disabled = true;
                fileInput.classList.add('hidden');
                progressContainer.classList.remove('hidden');
                statusText.textContent = "Starting job...";
                
                const formData = new FormData();
                formData.append('file', file);
                
                try {
                    // Read real branchId from session
                    const branchId = parseInt(localStorage.getItem('user_branch')) || 1;
                    const res = await apiMultipart(`/import/${moduleName}?branchId=${branchId}&userId=0`, 'POST', formData);
                    const jobId = typeof res === 'object' ? (res.data || res.jobId) : res;
                    pollProgress(jobId, onSuccess);
                } catch (err) {
                    uploadBtn.disabled = false;
                    progressContainer.classList.add('hidden');
                    showErrorMessage(err.message || "Failed to start import");
                }
            });
            
            modalEl.classList.remove('hidden');
        }
    };
})();
