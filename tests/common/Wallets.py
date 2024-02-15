import requests

class WalletServiceEndpoints:
    walletServiceURL = "http://localhost:8082"

    def create_wallet(self, user_id):
        requests.put(self.walletServiceURL+f"/wallets/{user_id}", json={"action":"credit", "amount":0})

    def get_wallet(self, user_id):
        response = requests.get(self.walletServiceURL + f"/wallets/{user_id}")
        return response

    def update_wallet(self, user_id, action, amount):
        response = requests.put(self.walletServiceURL + f"/wallets/{user_id}", json={"action":action, "amount":amount})
        return response

