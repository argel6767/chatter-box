import os
import subprocess
from pathlib import Path
import platform

'''
loads env variables for the desired environment of the backend Dev or Prod
'''

backend = Path.cwd()/"api"
is_os_windows = platform.system() == "Windows"
    
def load_env_file(application_path):
    file_path = application_path/".env"
    with open(file_path) as f:
        for line in f:
            if line.startswith("#") or not line.strip():
                continue
            key, _, value = line.strip().partition("=")
            os.environ[key] = value
            print(f"Set environment variable: {key}={value}")
            
def find_maven_home():
    maven_home = os.environ.get('MAVEN_HOME')
    if not maven_home:
        raise EnvironmentError("MAVEN_HOME environment variable is not set")
    
    if is_os_windows:
        mvn_path = Path(maven_home) / 'bin' / 'mvn.cmd'
    else:
        mvn_path = Path(maven_home) / 'bin' / 'mvn'
        
    return mvn_path

def main():
    load_env_file(backend)
    mvn_path = find_maven_home()
    print(mvn_path)
    # Run the Spring Boot application
    process = subprocess.run([mvn_path, "spring-boot:run"], cwd=str(backend), shell=is_os_windows)
    
    print(process)

if __name__ == "__main__":
    main()
