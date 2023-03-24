Releasing
========

The release process consists of first publishing a release candidate and then using
that release candidate to first publish a new snapshot and then the final release. This
is a final test that publishing generally works with the new release.

 1. Make sure `CHANGELOG.md` is up-to-date on `main` for the impeding release.
 2. `git tag -a X.Y.X-rc1 -m "Version X.Y.Z Release Candidate 1"` (where X.Y.Z is the new version)
 3. `git push --tags`
 4. Update `libs.versions.toml` to use `X.Y.X-rc1`
 5. `git commit -am "Update to X.Y.X-rc1"`
 6. `dependency-watch await com.vanniktech:gradle-maven-publish-plugin:X.Y.X-rc1 && git push`
 7. Wait for snapshot to be published successfully
 8. `git tag -a X.Y.X -m "Version X.Y.Z"`
 9. `git push --tags`

If the snapshot publishing fails in step 7 or the final release publishing after step 9 fails:
 1. Fix the issue that caused the failure
 2. Downgrade back to stable publish plugin
 3. Start the whole process again and bump the `rc` version by 1
