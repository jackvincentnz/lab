#!/usr/bin/env bash

set -euo pipefail

readonly vitest_binary_rootpath="$1"
shift

readonly workdir="$1"
shift

readonly workspace_root="${TEST_SRCDIR}/${TEST_WORKSPACE}"

if [[ -n "${workdir}" && "${workdir}" != "." ]]; then
  cd "${workspace_root}/${workdir}"
else
  cd "${workspace_root}"
fi

readonly vitest_binary="${workspace_root}/${vitest_binary_rootpath}"

args=("run" "$@")
coverage_report_destination=""
reports_dir=""
if [[ -n "${COVERAGE_DIR:-}" ]]; then
  coverage_report_destination="${COVERAGE_DIR}/coverage.dat"
elif [[ -n "${COVERAGE_OUTPUT_FILE:-}" ]]; then
  coverage_report_destination="${COVERAGE_OUTPUT_FILE}"
fi

if [[ -n "${coverage_report_destination}" ]]; then
  reports_dir="${TEST_TMPDIR}/vitest-coverage"
  rm -rf "${reports_dir}"
  mkdir -p "${reports_dir}"
  args=(
    "${args[@]}"
    "--coverage"
    "--coverage.reporter=lcov"
    "--coverage.reportsDirectory=${reports_dir}"
  )
fi

if [[ -n "${coverage_report_destination}" ]]; then
  env -u COVERAGE -u COVERAGE_DIR -u COVERAGE_MANIFEST -u COVERAGE_OUTPUT_FILE \
    "${vitest_binary}" "${args[@]}"
else
  "${vitest_binary}" "${args[@]}"
fi

if [[ -n "${coverage_report_destination}" ]]; then
  readonly lcov_report="${reports_dir}/lcov.info"
  readonly rewritten_lcov_report="${reports_dir}/lcov.rewritten.raw.info"
  readonly normalized_lcov_report="${reports_dir}/lcov.rewritten.info"
  readonly baseline_coverage_report="$(dirname "${coverage_report_destination}")/../baseline_coverage.dat"

  if [[ ! -f "${lcov_report}" ]]; then
    echo "Expected coverage report at ${lcov_report}" >&2
    exit 1
  fi

  while IFS= read -r line; do
    if [[ "${line}" == SF:src/* || "${line}" == SF:${workdir}/src/* ]]; then
      rel_path="${line#SF:}"
      if [[ "${rel_path}" == src/* ]]; then
        repo_rel_path="${workdir}/${rel_path}"
      else
        repo_rel_path="${rel_path}"
      fi
      rewritten_path="${repo_rel_path}"
      source_path="${workspace_root}/${repo_rel_path}"

      if [[ "${repo_rel_path}" == *.js ]]; then
        if [[ -f "${baseline_coverage_report}" ]]; then
          mapped_path="$(grep -E "^SF:${repo_rel_path%.js}\\.(ts|tsx)$" "${baseline_coverage_report}" | sed 's/^SF://;q')"
        else
          mapped_path=""
        fi

        if [[ -n "${mapped_path}" ]]; then
          rewritten_path="${mapped_path}"
        else
          source_base="${source_path%.js}"
          if [[ -f "${source_base}.tsx" ]]; then
            rewritten_path="${repo_rel_path%.js}.tsx"
          elif [[ -f "${source_base}.ts" ]]; then
            rewritten_path="${repo_rel_path%.js}.ts"
          fi
        fi
      fi

      printf 'SF:%s\n' "${rewritten_path}" >> "${rewritten_lcov_report}"
      continue
    fi

    printf '%s\n' "${line}" >> "${rewritten_lcov_report}"
  done < "${lcov_report}"

  awk -v workspace_root="${workspace_root}" '
    function file_max_line(file,    cmd, result) {
      if (!(file in file_line_counts)) {
        cmd = "wc -l < \"" workspace_root "/" file "\""
        result = 0
        if ((cmd | getline result) > 0) {
          file_line_counts[file] = result + 0
        } else {
          file_line_counts[file] = 0
        }
        close(cmd)
      }
      return file_line_counts[file]
    }

    function reset_record(    i) {
      tn_line = ""
      sf_line = ""
      fn_count = 0
      br_count = 0
      delete fn_lines
      delete fn_names
      delete fnda_hits
      delete raw_da_hits
      delete br_lines
      delete br_records
      delete br_hits
      delete normalized_da_hits
      delete normalized_branch_lines
    }

    function flush_record(    max_line, i, line, line_num, name, hits, line_found, line_hit, fn_found, fn_hit, br_found, br_hit) {
      if (sf_line == "") {
        reset_record()
        return
      }

      max_line = file_max_line(sf_line)

      if (tn_line != "") {
        print tn_line
      }
      print "SF:" sf_line

      fn_found = 0
      fn_hit = 0
      for (i = 1; i <= fn_count; i++) {
        if (fn_lines[i] > max_line) {
          continue
        }
        print "FN:" fn_lines[i] "," fn_names[i]
        fn_found++
        if (fnda_hits[fn_names[i]] > 0) {
          fn_hit++
        }
      }
      for (i = 1; i <= fn_count; i++) {
        if (fn_lines[i] > max_line) {
          continue
        }
        hits = fnda_hits[fn_names[i]]
        print "FNDA:" hits "," fn_names[i]
      }
      print "FNF:" fn_found
      print "FNH:" fn_hit

      br_found = 0
      br_hit = 0
      for (i = 1; i <= br_count; i++) {
        if (br_lines[i] > max_line) {
          continue
        }
        print br_records[i]
        normalized_branch_lines[br_lines[i]] = 1
        br_found++
        if (br_hits[i] != "-" && br_hits[i] + 0 > 0) {
          br_hit++
        }
      }
      print "BRF:" br_found
      print "BRH:" br_hit

      for (line in raw_da_hits) {
        line_num = line + 0
        if (line_num <= max_line) {
          normalized_da_hits[line_num] = raw_da_hits[line]
        }
      }
      for (line in normalized_branch_lines) {
        line_num = line + 0
        if (!(line_num in normalized_da_hits)) {
          normalized_da_hits[line_num] = 0
        }
      }
      for (i = 1; i <= fn_count; i++) {
        if (fn_lines[i] <= max_line && !(fn_lines[i] in normalized_da_hits)) {
          normalized_da_hits[fn_lines[i]] = 0
        }
      }

      line_found = 0
      line_hit = 0
      for (line in normalized_da_hits) {
        print "DA:" line "," normalized_da_hits[line]
        line_found++
        if (normalized_da_hits[line] > 0) {
          line_hit++
        }
      }
      print "LH:" line_hit
      print "LF:" line_found
      print "end_of_record"

      reset_record()
    }

    BEGIN {
      reset_record()
    }

    /^TN:/ {
      tn_line = $0
      next
    }

    /^SF:/ {
      sf_line = substr($0, 4)
      next
    }

    /^FN:/ {
      split(substr($0, 4), fn_parts, ",")
      fn_count++
      fn_lines[fn_count] = fn_parts[1] + 0
      fn_names[fn_count] = fn_parts[2]
      next
    }

    /^FNDA:/ {
      split(substr($0, 6), fnda_parts, ",")
      fnda_hits[fnda_parts[2]] = fnda_parts[1] + 0
      next
    }

    /^DA:/ {
      split(substr($0, 4), da_parts, ",")
      raw_da_hits[da_parts[1]] = da_parts[2] + 0
      next
    }

    /^BRDA:/ {
      split(substr($0, 6), br_parts, ",")
      br_count++
      br_lines[br_count] = br_parts[1] + 0
      br_hits[br_count] = br_parts[4]
      br_records[br_count] = $0
      next
    }

    /^end_of_record$/ {
      flush_record()
    }

    END {
      flush_record()
    }
  ' "${rewritten_lcov_report}" > "${normalized_lcov_report}"

  mkdir -p "$(dirname "${coverage_report_destination}")"
  cp "${normalized_lcov_report}" "${coverage_report_destination}"
fi
