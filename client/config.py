import json

CONFIG_FILE = '../config.json'


def get_config(config_file=CONFIG_FILE):
    """
    Load the configuration from the config file.

    Returns:
        dict: The configuration values (host, port, max_msg)
    """
    with open(config_file, "r") as f:
        config = json.load(f)

    servers = config["SERVERS"]
    max_msg = config["MAX_MSG_TO_DISPLAY"]
    max_users = config["MAX_USERS_TO_DISPLAY"]

    return {"servers": servers, "max_msg": max_msg, "max_users": max_users}
