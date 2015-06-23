ALTER TABLE NoteItems ADD COLUMN status INTEGER; 
UPDATE NoteItems SET status = 0 WHERE status IS NULL;