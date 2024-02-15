import requests

class UserServiceEndpoints:
    userServiceURL = "http://localhost:8080"

    def create_user(self, name, email):
        new_user = {"name": name, "email": email}
        response = requests.post(self.userServiceURL + "/users", json=new_user)
        return response

    def get_userid(self, user_id):
        response = requests.get(self.userServiceURL + f"/users/{user_id}")
        return response

    def delete_users(self):
        requests.delete(self.userServiceURL+f"/users")




