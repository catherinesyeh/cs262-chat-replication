from network import ChatClient
import config
from ui import ChatUI
import tkinter as tk


def main():
    print("Starting client...")
    client_config = config.get_config()
    servers = client_config["servers"]
    max_msg = client_config["max_msg"]
    max_users = client_config["max_users"]

    # Set up a ChatClient instance and connect to the server
    print(
        f"Configuration: \nservers={servers}, \nmax_msg={max_msg}, \nmax_users={max_users}")

    # Create a client
    client = ChatClient(servers, max_msg, max_users)

    # Start the user interface, passing in existing client
    root = tk.Tk()
    ChatUI(root, client)
    root.mainloop()


if __name__ == "__main__":
    main()
