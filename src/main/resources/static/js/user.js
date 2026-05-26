const params = new URLSearchParams(window.location.search);
const updateUserForm = document.getElementById("updateUserForm");
if (params.get("paymentSuccess")) {
  showToast("Payment successful", "success");
}

updateUserForm.addEventListener("submit", async function (event) {
  event.preventDefault();
  const token = localStorage.getItem("token");
  const email = document.getElementById("updateEmail").value;
  const firstName = document.getElementById("updateFirstName").value;
  const lastName = document.getElementById("updateLastName").value;
  const currentPassword = document.getElementById("currentPassword").value;
  const password = document.getElementById("updatePassword").value;
  const repeatedPassword = document.getElementById(
    "updateRepeatedPassword",
  ).value;
  const updateUserForm = document.getElementById("updateUserForm");
  const body = {};

  if (email.trim() !== "") {
    body.email = email;
  }
  if (firstName.trim() !== "") {
    body.firstName = firstName;
  }
  if (lastName.trim() !== "") {
    body.lastName = lastName;
  }
  if (
    currentPassword.trim() !== "" ||
    password.trim() !== "" ||
    repeatedPassword.trim() !== ""
  ) {
    body.currentPassword = currentPassword;
    body.password = password;
    body.repeatedPassword = repeatedPassword;
  }
  if (Object.keys(body).length === 0) {
      showToast("Nothing was changed because no data was entered.", "info");
      return;
    }
    
    try {
      const response = await fetch(`${API_URL}/users/me`, {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify(body),
      });
      if (response.ok) {
      showToast("Profile updated!", "info");
      updateUserForm.reset();
      loadUserData();
    } else {
      const errorData = await response.json();
      showToast("Update failed: " + errorData.message, "danger");
    }
  } catch (error) {
    const errorText = await response.text();
    console.error(errorText);
    showToast("Server error", "danger");
  }
});

async function loadUserData() {
  const token = localStorage.getItem("token");
  
  if (!token) {
    window.location.href = "index.html";
    return;
  }
  
  try {
    const response = await fetch(`${API_URL}/users/me`, {
      method: "GET",
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    if (!response.ok) {
      throw new Error("Unauthorized");
    }
    const user = await response.json();
    document.getElementById("userEmail").innerText = user.email;
    document.getElementById("userFirstName").innerText = user.firstName;
    document.getElementById("userLastName").innerText = user.lastName;
  } catch (error) {
    console.error(error);
    localStorage.removeItem("token");
    window.location.href = "index.html";
  }
}

async function loadBookings() {
  const token = localStorage.getItem("token");
  
  try {
    const response = await fetch(`${API_URL}/bookings/my`, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    
    const bookings = await response.json();
    const noBookingsMessage = document.getElementById("noBookingsMessage");
    if (bookings.length != 0) {
      noBookingsMessage.style.display = "none";
    } else {
      noBookingsMessage.style.display = "block";
    }
    renderBookings(bookings);
  } catch (error) {
    console.error(error);
  }
}

function renderBookings(bookings) {
  const container = document.getElementById("bookingsContainer");
  container.innerHTML = "";
  bookings.forEach((booking) => {
    container.innerHTML += `
    <div class="col-lg-6 mb-3">
    <div class="card p-3">
    <h4>${booking.city}</h4>
    <p>${booking.street}</p>
    <img class="card-img-top rounded-4 accommodation-image" src="images/accommodations/accommodation_${booking.accommodationId}.jpg">
    <p>${booking.checkInDate} - ${booking.checkOutDate}</p>
    <p>Status: ${booking.status}</p>
    ${
      booking.status === "PENDING"
      ? `
          <button class="btn btn-primary" onclick="payBooking(${booking.id})">Pay</button> `
              : `
          <span class="badge bg-success">Paid</span>
          `
          }
       </div>
     </div>
     `;
    });
  }
  
  async function payBooking(bookingId) {
    const token = localStorage.getItem("token");
    
    try {
      const response = await fetch(`${API_URL}/payments`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${token}`,
        },
        body: JSON.stringify({ bookingId: bookingId }),
      });
      
      if (response.ok) {
        const payment = await response.json();
        window.location.href = payment.sessionUrl;
      } else {
        const error = await response.json();
        showToast(error.message, "danger");
    }
  } catch (error) {
    console.error(error);
  }
}

loadUserData();
updateUserForm.reset();
loadBookings();
