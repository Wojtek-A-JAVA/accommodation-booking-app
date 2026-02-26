UPDATE accommodations SET is_deleted = false WHERE id = 1;
INSERT INTO amenities_accommodations (accommodation_id, amenity_id) VALUES (1, 1);
INSERT INTO amenities_accommodations (accommodation_id, amenity_id) VALUES (1, 3);
INSERT INTO amenities_accommodations (accommodation_id, amenity_id) VALUES (1, 4);
INSERT INTO amenities_accommodations (accommodation_id, amenity_id) VALUES (1, 6);