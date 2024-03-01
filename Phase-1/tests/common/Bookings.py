import requests

class BookingServiceEndpoints:
    bookingServiceURL = "http://localhost:8081"

    def get_show_details(self, show_id):
        response = requests.get(self.bookingServiceURL + f"/shows/{show_id}")
        return response


    def delete_show(self, user_id,show_id):
        response = requests.delete(self.bookingServiceURL+f"/bookings/users/{user_id}/shows/{show_id}")
        return response

