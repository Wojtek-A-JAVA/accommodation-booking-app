let locations = [];
async function loadAccommodations() {
  try {
    const accommodationsResponse = await fetch(`${API_URL}/accommodations`);
    const accommodations = await accommodationsResponse.json();
    const locationsResponse = await fetch(`${API_URL}/locations`);
    locations = await locationsResponse.json();
    renderAccommodations(accommodations, locations);
    renderLocations(locations);
  } catch (error) {
    console.error(error);
  }
}

function renderAccommodations(
  accommodations,
  locations,
  isSearch = false,
  checkIn = "",
  checkOut = "",
) {
  const container = document.getElementById("accommodationsContainer");
  container.innerHTML = "";
  accommodations.forEach((accommodation) => {
    const location = locations.find((l) => l.id == accommodation.locationId);
    let imageHtml = `<img class="card-img-top rounded-4 accommodation-image" src="images/accommodations/accommodation_${accommodation.id}.jpg">`;
    if (isSearch) {
      imageHtml = `<a onclick="handleBookingClick(${accommodation.id}, '${checkIn}', '${checkOut}')">
                   <img class="card-img-top rounded-4 accommodation-image" style="cursor: pointer" src="images/accommodations/accommodation_${accommodation.id}.jpg">
                  </a>`;
    }
    container.innerHTML += `
        <div class="col-md-6 col-lg-4 mb-3">
          <div class="card">
            <div class="card-body p-0">
              ${imageHtml}
              <h3 class="pt-4">
                  $${accommodation.dailyRate}
              <span class="text-muted fs-5">/day</span>
              </h3>
              <h4 class="card-title">${accommodation.type}</h4>
              <p class="card-text">
                ${location.country},
                ${location.street},
                ${location.city}
              </p>
              <p>Size: ${accommodation.size}</p>
              <p>
                Amenities:
                 ${accommodation.amenityIds
                   .map(
                     (id) => `
                <img src="images/amenities/amenity_${id}.svg" class="logo-amenity">
                  `,
                   )
                   .join("")}
              </p>
            </div>
          </div>
        </div>
    `;
  });
}

function renderLocations(locations) {
  const select = document.getElementById("locationSelect");
  const cities = [...new Set(locations.map((location) => location.city))];
  cities.forEach((city) => {
    select.innerHTML += `
            <option value="${city}">
                ${city}
            </option>
        `;
  });
}

async function searchAccommodations() {
  const city = document.getElementById("locationSelect").value;
  const checkIn = document.getElementById("checkIn").value;
  const checkOut = document.getElementById("checkOut").value;
  if (checkOut <= checkIn) {
    showToast("Check-out date must be after check-in date.", "warning");
    return;
  }
  if (!checkIn || !checkOut) {
    showToast("Please select dates.", "warning");
    return;
  }
  if (!city) {
    showToast(
      "No city selected. Showing all accommodations available for the selected dates.",
      "info",
    );
  }
  try {
    const response = await fetch(
      `${API_URL}/accommodations/search?city=${city}&checkIn=${checkIn}&checkOut=${checkOut}`,
    );
    const accommodations = await response.json();
    renderAccommodations(accommodations, locations, true, checkIn, checkOut);
    if (city) {
      document.getElementById("accommodadionsH2").innerText =
        `Available Properties in ${city}`;
    } else {
      document.getElementById("accommodadionsH2").innerText =
        "Available Properties";
    }
    document.getElementById("accommodadionsP").style.display = "block";
  } catch (error) {
    console.error(error);
  }
}

document
  .getElementById("searchButton")
  .addEventListener("click", searchAccommodations);

function handleBookingClick(accommodationId, checkIn, checkOut) {
  const token = localStorage.getItem("token");

  if (!token) {
    localStorage.setItem(
      "pendingBooking",

      JSON.stringify({
        accommodationId,
        checkIn,
        checkOut,
      }),
    );

    const loginModal = new bootstrap.Modal(
      document.getElementById("authModal"),
    );

    loginModal.show();

    return;
  }

  openBookingModal(accommodationId, checkIn, checkOut);
}

loadAccommodations();
