import sys
from common.Wallets import WalletServiceEndpoints
from common.Users import UserServiceEndpoints

# Object for accessing User and Wallet end points
userserv = UserServiceEndpoints()
walletserv = WalletServiceEndpoints()

## --------------------- Test Case Routines ---------------------
def TC1_nonexistant_wallet() -> bool:
    response = walletserv.get_wallet(0)
    if (response.status_code == 404):
        return True
    return False

def TC2_existant_wallet() -> bool:
    user_name_list = ['Aragon', 'Bilbo', 'Gandalf']
    user_email_list = ['aragon@iisc.ac.in', 'bilbo@gmail.com', 'gandalf@gmail.com']
    user_wallet_amount_list = [0, 1000, 2000]

    for idx in range(len(user_name_list)):
        response_usercreate = userserv.create_user(user_name_list[idx], user_email_list[idx])

        if (response_usercreate.status_code == 201):
            response = walletserv.create_wallet(response_usercreate.json()['id'], user_wallet_amount_list[idx])

            if (response.status_code != 200):
                print("Wallet creation failed - returned ", response.status_code)
                return False

            response = walletserv.get_wallet(response_usercreate.json()['id'])
            if (response_usercreate.json()['id'] != response.json()['user_id']):
                print("Wallet creation failed - wallet userid mismatch ")
                return False

            if (user_wallet_amount_list[idx] != response.json()['balance']):
                print("Wallet creation failed - wallet amount mismatch ")
                return False

        else:
            print("User ID creation failed - obtained non-201 response")
            return False

    return True

def TC3_wallet_debit_transaction() -> bool:
    user_name_list = ['Aragon', 'Bilbo', 'Gandalf']
    user_email_list = ['aragon@iisc.ac.in', 'bilbo@gmail.com', 'gandalf@gmail.com']
    user_wallet_amount_list = [0, 1000, 2000]
    user_id_list = []
    user_wallet_transact_amount = [250, 500, 1500]
    user_wallet_transact_response_expect = [400, 200, 200]

    userserv.delete_users()
    #########################################################
    # Create the accounts and load their wallets
    #########################################################
    for idx in range(len(user_name_list)):
        response_usercreate = userserv.create_user(user_name_list[idx], user_email_list[idx])

        if (response_usercreate.status_code == 201):
            response = walletserv.create_wallet(response_usercreate.json()['id'], user_wallet_amount_list[idx])
            user_id_list.append(response_usercreate.json()['id'])
            if (response.status_code != 200):
                print("Wallet creation failed - returned ", response.status_code)
                return False

            response = walletserv.get_wallet(user_id_list[idx])
            if (user_id_list[idx] != response.json()['user_id']):
                print("Wallet creation failed - wallet userid mismatch ")
                return False

            if (user_wallet_amount_list[idx] != response.json()['balance']):
                print("Wallet creation failed - wallet amount mismatch ")
                return False
        else:
            print("User ID creation failed - obtained non-201 response")
            return False
    #########################################################
    # Perform debit transactions
    #########################################################
    for idx in range(len(user_id_list)):
        response = walletserv.update_wallet(user_id_list[idx], "debit", user_wallet_transact_amount[idx])
        if (response.status_code != user_wallet_transact_response_expect[idx]):
            print("Wallet debit transaction failed - returned ", response.status_code,
                  ", expected ", user_wallet_transact_response_expect[idx])
            return False
        if (user_wallet_amount_list[idx] < 0):
            remaining_amount = (user_wallet_amount_list[idx] - user_wallet_transact_amount[idx])
            if (response.json()['balance'] != remaining_amount):
                print("Wallet debit transaction failed - amount mismatch ", response.json()['balance'],
                      ", expected ", remaining_amount)
                return False

    return True


## ------------------------ Main Routine ------------------------
if __name__ == '__main__':
    userserv.delete_users()
    if (TC1_nonexistant_wallet() != True):
        print('TC1_nonexistant_wallet: Failed')
        sys.exit()
    print('TC1_nonexistant_wallet: Passed')

    if (TC2_existant_wallet() != True):
        print('TC2_existant_wallet: Failed')
        sys.exit()
    print('TC2_existant_wallet: Passed')

    if (TC3_wallet_debit_transaction() != True):
        print('TC3_wallet_debit_transaction: Failed')
        sys.exit()
    print('TC3_wallet_debit_transaction: Passed')

