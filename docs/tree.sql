WITH RECURSIVE tree AS (
    SELECT
        id,
        parent,
        coalesce(title, id) as title,
        0 AS level,
        lower(id) AS sort_path       
    FROM items
    WHERE parent = 'ROOT'
    UNION ALL
    SELECT
        n.id,
        n.parent,
        coalesce(n.title, n.id) as title,
        tree.level + 1 AS level,
        tree.sort_path || '>' || lower(n.id) AS sort_path
    FROM items AS n
    JOIN tree ON n.parent = tree.id
)
SELECT
    id,
    parent,
    title,
    level
FROM tree
WHERE id in
	 (
	 WITH RECURSIVE ancestors AS (
		SELECT
			id,
			parent
		FROM items
		WHERE id in (select item from shop_items where shop = 'apocat')
		UNION ALL
		SELECT
			n.id,
			n.parent
		FROM items AS n
		JOIN ancestors AS a ON n.id = a.parent
	)
	SELECT id
    FROM ancestors
)
ORDER BY sort_path;