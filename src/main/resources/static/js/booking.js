const params = new URLSearchParams(window.location.search);
const accommodationId = params.get("id");
const checkIn = params.get("checkIn");
const checkOut = params.get("checkOut");

async function openBookingModal(accommodationId, checkIn, checkOut) {
  const token = localStorage.getItem("token");

  const responseAccomodation = await fetch(
    `${API_URL}/accommodations/${accommodationId}`,
    {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    },
  );
  const accommodation = await responseAccomodation.json();
  const location = locations.find((l) => l.id == accommodation.locationId);
  const startDate = new Date(checkIn);
  const endDate = new Date(checkOut);
  const totalDays = (endDate - startDate) / (1000 * 60 * 60 * 24);
  const totalPrice = totalDays * accommodation.dailyRate;

  document.getElementById("bookingModalBody").innerHTML = `
        <div class="">
            <div class="card">
                <div class="card-body p-0">
                   <img class="card-img-top rounded-4" src="images/accommodations/accommodation_${accommodation.id}.jpg">
                   <h4 class="card-title">${accommodation.type}</h4>
                   <p class="card-text">
                     ${location.country},
                     ${location.street},
                     ${location.city}
                    </p>
                    <h5 class="card-title">
                     ${checkIn} - ${checkOut} <br>
                     Total days: ${totalDays} <br>
                     Total price: ${totalPrice} $
                    </h5>
                </div>
            </div>
        </div>
        <button class="btn btn-primary" onclick=" confirmBooking(${accommodation.id}, '${checkIn}', '${checkOut}')">
          Confirm Booking
        </button>
    `;

  const modal = new bootstrap.Modal(document.getElementById("bookingModal"));
  modal.show();
}

async function confirmBooking(accommodationId, checkIn, checkOut) {
  const token = localStorage.getItem("token");

  try {
    const response = await fetch(`${API_URL}/bookings`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
      },
      body: JSON.stringify({
        accommodationId: accommodationId,
        checkInDate: checkIn,
        checkOutDate: checkOut,
      }),
    });

    if (response.ok) {
      showToast("Booking created successfully", "success");
      window.location.href = "user.html";
    } else {
      const error = await response.json();
      showToast(error.message, "danger");
    }
  } catch (error) {
    console.error(error);
    showToast("Server error", "danger");
  }
}
