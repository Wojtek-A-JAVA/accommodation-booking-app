function showToast(message, type = "dark") {
  const toastElement = document.getElementById("appToast");
  const toastMessage = document.getElementById("toastMessage");
  const toast = new bootstrap.Toast(toastElement);
  toastMessage.textContent = message;
  toastElement.className = `toast align-items-center text-bg-${type} border-0`;
  toast.show();
}
