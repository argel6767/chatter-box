from pathlib import Path
import platform
import subprocess
import sys
import time

# Your existing setup code
api = Path.cwd() / "api"
isOSWindows = platform.system() == "Windows"


# check if the docker daemon is running before anything is done
def check_if_docker_is_running():
    docker_check = subprocess.run(["docker", "info"], capture_output=True, shell=isOSWindows)
    if docker_check.returncode != 0:
        print("ERROR: Docker is not running. Please start Docker Desktop.")
        sys.exit(1)


def build_image():
    print('Build image')

    # Generate a unique timestamp tag
    tag = str(int(time.time()))
    timestamp_image = f"ahernandezam/chatter-box-api:v{tag}"
    latest_image = f"ahernandezam/chatter-box-api:latest"

    print(f"Building Image with tag: {tag}\n\n")
    build_image_process = subprocess.run(["docker", "build", "-t", timestamp_image, "."], cwd=str(api), shell=isOSWindows)
    print(build_image_process)
    print("Image built\n\n")

    print("Tagging image as 'latest'...")
    tag_process = subprocess.run(
        ["docker", "tag", timestamp_image, latest_image],
        shell=isOSWindows
    )

    return timestamp_image


def main():
    check_if_docker_is_running()
    build_image()


if __name__ == '__main__':
    main()
