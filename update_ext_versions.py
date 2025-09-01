import re
import xml.etree.ElementTree as ET
import requests
from pathlib import Path

EXT_DEPENDENCIES = {
    "awsVersion": ("software.amazon.awssdk", "ec2"),
    "jUnitJupiterVersion": ("org.junit.jupiter", "junit-jupiter-api"),
    "jUnitPlatformVersion": ("org.junit.platform", "junit-platform-engine"),
    "lombokVersion": ("org.projectlombok", "lombok"),
}

def get_latest_version(group_id, artifact_id):
    """
    Fetches the latest released version of the artifact from Maven Central metadata.
    """
    base_url = "https://repo1.maven.org/maven2"
    path = f"{group_id.replace('.', '/')}/{artifact_id}/maven-metadata.xml"
    url = f"{base_url}/{path}"

    try:
        response = requests.get(url, timeout=10)
        response.raise_for_status()
        xml = ET.fromstring(response.text)
        version = xml.findtext("./versioning/release") or xml.findtext("./versioning/latest")
        return version.strip() if version else None
    except Exception as e:
        print(f"Error fetching metadata for {group_id}:{artifact_id} → {e}")
        return None

def update_ext_versions(build_gradle_path: Path):
    """
    Updates the versions inside the ext block of build.gradle.
    """
    original = build_gradle_path.read_text()
    updated = original

    for var_name, (group_id, artifact_id) in EXT_DEPENDENCIES.items():
        pattern = re.compile(rf'{var_name}\s*=\s*[\'"]([^\'"]+)[\'"]')
        match = pattern.search(original)

        if not match:
            print(f"{var_name} not found in build.gradle")
            continue

        current_version = match.group(1)
        latest_version = get_latest_version(group_id, artifact_id)

        if not latest_version:
            print(f"Skipping {var_name}: failed to get latest version.")
            continue

        if current_version != latest_version:
            print(f"Updating {var_name}: {current_version} → {latest_version}")
            updated = pattern.sub(f'{var_name} = \'{latest_version}\'', updated)
        else:
            print(f"{var_name} is up to date ({current_version})")

    if updated != original:
        build_gradle_path.write_text(updated)
        print("build.gradle updated.")
    else:
        print("No changes made. All ext versions are up to date.")

if __name__ == "__main__":
    build_gradle_file = Path("build.gradle")
    if not build_gradle_file.exists():
        print("build.gradle not found.")
    else:
        update_ext_versions(build_gradle_file)
