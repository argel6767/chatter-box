from run_server import load_env_file
import subprocess
from pathlib import Path
import platform

'''
loads env file and runs email service
'''
email_service = Path.cwd()/"chatterbox-email-service"
is_os_windows = platform.system() == "Windows"

def main():
    load_env_file(email_service)
    process = subprocess.run(["quarkus", "dev", "-Dquarkus.http.port=8086"], cwd=email_service, stdout=subprocess.PIPE, stderr=subprocess.PIPE, shell=is_os_windows)
    print(process)

if __name__ == "__main__":
    main()