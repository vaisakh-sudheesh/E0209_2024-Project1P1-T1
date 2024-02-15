import requests

class WalletServiceEndpoints:
    walletServiceURL = "http://localhost:8082"

    def create_wallet(self, user_id, amount_=0):
        response = requests.put(self.walletServiceURL+f"/wallets/{user_id}", json={"action":"credit", "amount": amount_})
        return response

    def get_wallet(self, user_id):
        response = requests.get(self.walletServiceURL + f"/wallets/{user_id}")
        return response

    def update_wallet(self, user_id, action, amount):
        response = requests.put(self.walletServiceURL + f"/wallets/{user_id}", json={"action":action, "amount":amount})
        return response

