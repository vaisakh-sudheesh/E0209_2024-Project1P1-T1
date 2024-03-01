from common.Users import UserServiceEndpoints
import sys

def test_create_user(name, email, response, check_response=True):
    new_user = response.json()

    if ('id' not in new_user):
        print("create_user() Failed: 'id' field not present in response.")
        return False
    else:
        if (not isinstance(new_user['id'], int)):
            print("create_user() Failed: 'id' field not an integer value.")
            return False

    if ('name' not in new_user):
        print("create_user() Failed: 'name' field not present in response.")
        return False
    elif (new_user['name'] != name):
        print("create_user() Failed: 'name' field incorrect in response.")
        return False

    if ('email' not in new_user):
        print("create_user() Failed: 'email' field not present in response.")
        return False
    elif (new_user['email'] != email):
        print("create_user() Failed: 'email' field incorrect in response.")
        return False

    if (len(new_user) != 3):
        print("create_user() Failed: Incorrect response format.")
        return False

    if (check_response):
        if (response.status_code != 201):
            print(f"create_user() Failed: HTTP 201 expected, got {response.status_code}")
            return False
    return True



if __name__ == '__main__':
    userserv = UserServiceEndpoints()

    userserv.delete_users()
    user_name_list = ['Aragon', 'Bilbo', 'Gandalf']
    user_email_list = [ 'aragon@iisc.ac.in', 'bilbo@gmail.com', 'gandalf@gmail.com']

    # Normal insertion test
    for idx in range(len(user_name_list)):
        response = userserv.create_user(user_name_list[idx], user_email_list[idx])
        if (response.status_code == 201):
            if (test_create_user(user_name_list[idx], user_email_list[idx], response) != True):
                print("User ID creation test failed")
                sys.exit()
        else:
            print("User ID creation test failed - obtained non-201 response")
            sys.exit()
    print("User ID creation test passed - no mismatches")

    # Try inserting an existing one, does it fail?
    response = userserv.create_user(user_name_list[0], user_email_list[0])
    if (response.status_code == 201):
        print("Duplicate ID creation test failed, obtained anything other than 201")
        sys.exit()
    print("Duplicate ID creation test passed")

    userserv.delete_users()

    response = userserv.create_user(user_name_list[0], user_email_list[0])
    if (response.status_code == 201):
        newuserid = response.json()['id']

        #print('newuserid :', newuserid)
        response = userserv.get_userid(newuserid)
        if (test_create_user(user_name_list[0], user_email_list[0], response, False) != True):
            print("Get User id test failed")
            sys.exit()
        if (newuserid != response.json()['id']):
            print(f"Get User id test failed - ID mismatch, expected {newuserid}, obtained {response.json()['id']}")
            sys.exit()
        print("Get User id test passed")

