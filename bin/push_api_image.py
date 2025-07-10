import subprocess
from pathlib import Path
import platform
import http.client
import json
import ssl
import os


from build_api_image import build_image, check_if_docker_is_running

api = Path.cwd()/"api"
is_os_windows = platform.system() == "Windows"

def load_sevalla_env_file():
    file_path = Path.cwd()/"sevalla.env"
    with open(file_path) as f:
        for line in f:
            if line.startswith("#") or not line.strip():
                continue
            key, _, value = line.strip().partition("=")
            os.environ[key] = value
            print(f"Set environment variable: {key}={value}")

def build_and_push_with_unique_tag():
    image_name = build_image()

    print(f"Pushing image {image_name} to Docker Registry\n\n")
    push_image = subprocess.run(["docker", "push", image_name], shell=is_os_windows)
    print(push_image)
    print("Image pushed\n\n")

    return image_name

def update_sevalla_deployment(image_name):
    print("Updating sevalla deployment")
    host = "api.sevalla.com"
    path = "/v2/applications/deployments"

    payload = {
        "app_id": os.environ["APP_ID"],
        "branch": "main",
        "docker_image": image_name,
        "is_restart": True
    }

    headers = {
        "Authorization": "Bearer " + os.environ["SEVALLA_API_KEY"],
        "Content-Type": "application/json"
    }

    # 1. Encode the payload to JSON string and then to bytes
    json_payload = json.dumps(payload)
    encoded_payload = json_payload.encode('utf-8')
    headers["Content-Length"] = str(len(encoded_payload))

    conn = None

    try:
        conn = http.client.HTTPSConnection(host, context=ssl.create_default_context())
        conn.request("POST", path, body=encoded_payload, headers=headers)
        response = conn.getresponse()
        response_body = response.read().decode('utf-8')
        print(f"Status: {response.status} {response.reason}")
        print("Response Body:")
        print(response_body)
        print("Deployment updated")

    except Exception as e:
        print(f"An error occurred: {e}")
        print("Deployment failed!")
    finally:
        if conn:
            conn.close()

def main():
    load_sevalla_env_file();
    check_if_docker_is_running()
    image_name = build_and_push_with_unique_tag()
    update_sevalla_deployment(image_name)


if __name__ == "__main__":
    main()