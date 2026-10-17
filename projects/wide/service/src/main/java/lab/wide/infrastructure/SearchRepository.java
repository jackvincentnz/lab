package lab.wide.infrastructure;

import java.util.List;
import java.util.UUID;
import lab.wide.domain.Signal;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.Repository;

/** Cross-entity read query; writes remain in the standard entity repositories. */
public interface SearchRepository extends Repository<Signal, UUID> {
  @Query(
      """
      WITH records AS (
        SELECT id, 'SIGNAL' AS type, title, captured_at AS updated_at, content,
          NULL::text AS impact, NULL::text AS desired_outcome, NULL::text AS scope,
          NULL::text AS tradeoffs, NULL::text AS effort, source
        FROM signal WHERE :includeSignals
        UNION ALL
        SELECT id, 'PROBLEM', title, updated_at, description, impact, desired_outcome,
          NULL, NULL, NULL, NULL FROM problem WHERE :includeProblems
        UNION ALL
        SELECT id, 'SOLUTION', title, updated_at, approach, NULL, NULL, scope, tradeoffs,
          effort, NULL FROM solution WHERE :includeSolutions
      ), selected AS (
        SELECT * FROM records r
        WHERE (:query = '' OR strpos(lower(concat_ws(E'\n', title, content, impact,
          desired_outcome, scope, tradeoffs, effort, source)), lower(:query)) > 0)
        AND (CAST(:problemId AS uuid) IS NULL OR
          (r.type = 'SOLUTION' AND EXISTS (SELECT 1 FROM solution_problem sp
            WHERE sp.solution_id = r.id AND sp.problem_id = :problemId)))
        ORDER BY updated_at DESC, type, id
        LIMIT :limit OFFSET :offset
      )
      SELECT r.*,
        CASE r.type
          WHEN 'PROBLEM' THEN (SELECT count(*) FROM problem_signal WHERE problem_id = r.id)
          WHEN 'SOLUTION' THEN (SELECT count(*) FROM solution_signal WHERE solution_id = r.id)
          ELSE 0 END AS signal_count,
        CASE r.type
          WHEN 'SIGNAL' THEN (SELECT count(*) FROM problem_signal WHERE signal_id = r.id)
          WHEN 'SOLUTION' THEN (SELECT count(*) FROM solution_problem WHERE solution_id = r.id)
          ELSE 0 END AS problem_count,
        CASE r.type
          WHEN 'SIGNAL' THEN (SELECT count(*) FROM solution_signal WHERE signal_id = r.id)
          WHEN 'PROBLEM' THEN (SELECT count(*) FROM solution_problem WHERE problem_id = r.id)
          ELSE 0 END AS solution_count,
        (SELECT count(*) FROM refinement_question q WHERE q.status = 'OPEN' AND
          ((r.type = 'PROBLEM' AND q.problem_id = r.id) OR
           (r.type = 'SOLUTION' AND q.solution_id = r.id))) AS open_question_count,
        (SELECT count(*) FROM refinement_decision d WHERE
          (r.type = 'PROBLEM' AND d.problem_id = r.id) OR
          (r.type = 'SOLUTION' AND d.solution_id = r.id)) AS decision_count
      FROM selected r ORDER BY updated_at DESC, type, id
      """)
  List<SearchRow> search(
      String query,
      boolean includeSignals,
      boolean includeProblems,
      boolean includeSolutions,
      UUID problemId,
      int limit,
      long offset);
}
