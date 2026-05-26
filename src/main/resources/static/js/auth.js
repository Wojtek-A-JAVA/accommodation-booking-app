const formRegister = document.getElementById("formRegister");
const formLogin = document.getElementById("formLogin");
const openLoginModal = document.getElementById("openLoginModal");
const openRegisterModal = document.getElementById("openRegisterModal");
const logoutButton = document.getElementById("logoutButton");

if (formRegister) {
  formRegister.addEventListener("submit", async function (event) {
    event.preventDefault();
    const email = document.getElementById("registerEmail").value;
    const firstName = document.getElementById("registerFirstName").value;
    const lastName = document.getElementById("registerLastName").value;
    const password = document.getElementById("registerPassword").value;
    const repeatedPassword = document.getElementById(
      "registerRepeatedPassword",
    ).value;
    const requestBody = {
      email,
      firstName,
      lastName,
      password,
      repeatedPassword,
    };
    try {
      const response = await fetch(`${API_URL}/auth/register`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(requestBody),
      });
      if (response.ok) {
        document.getElementById("loginEmail").value = email;
        document.getElementById("nav-sign-in-tab2").click();
      } else {
        const errorData = await response.json();
        showToast(errorData.message || "Registration failed", "danger");
      }
    } catch (error) {
      console.error(error);
      showToast("Server connection error", "danger");
    }
  });
}

if (formLogin) {
  formLogin.addEventListener("submit", async function (event) {
    event.preventDefault();
    const email = document.getElementById("loginEmail").value;
    const password = document.getElementById("loginPassword").value;

    try {
      const response = await fetch(`${API_URL}/auth/login`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          email,
          password,
        }),
      });

      if (response.ok) {
        const data = await response.json();
        const token = data.token;
        localStorage.setItem("token", token);
        const modalElement = document.getElementById("authModal");
        const modal = bootstrap.Modal.getInstance(modalElement);
        document.activeElement.blur();
        modal.hide();
        document.getElementById("openLoginModal").style.display = "none";
        document.getElementById("openRegisterModal").style.display = "none";
        document.getElementById("userMenu").style.display = "block";
        document.getElementById("logoutButton").style.display = "block";
      } else {
        showToast("Invalid credentials", "warning");
      }
    } catch (error) {
      console.error(error);
      showToast("Server error", "danger");
    }
  });
}
if (openLoginModal) {
  openLoginModal.addEventListener("click", function () {
    document.getElementById("nav-sign-in-tab2").click();
  });
}
if (openRegisterModal) {
  openRegisterModal.addEventListener("click", function () {
    document.getElementById("nav-register-tab2").click();
  });
}
if (logoutButton) {
  logoutButton.addEventListener("click", function (event) {
    event.preventDefault();
    logout();
  });
}

function updateNavigation() {
  const token = localStorage.getItem("token");
  const loginbtn = document.getElementById("openLoginModal");
  const registerbtn = document.getElementById("openRegisterModal");
  const userMenu = document.getElementById("userMenu");
  const logout = document.getElementById("logoutButton");

  if (token) {
    if (loginbtn) loginbtn.style.display = "none";
    if (registerbtn) registerbtn.style.display = "none";
    if (userMenu) userMenu.style.display = "block";
    if (logout) logout.style.display = "block";
  } else {
    if (loginbtn) loginbtn.style.display = "block";
    if (registerbtn) registerbtn.style.display = "block";
    if (userMenu) userMenu.style.display = "none";
    if (logout) logout.style.display = "none";
  }
}

function logout() {
  localStorage.removeItem("token");
  updateNavigation();
  window.location.href = "index.html";
}

updateNavigation();
