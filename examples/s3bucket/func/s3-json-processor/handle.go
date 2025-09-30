package function

import (
	"context"
	"encoding/json"
	"log"

	"github.com/cloudevents/sdk-go/v2/event"
)

func Handle(ctx context.Context, e event.Event) (*event.Event, error) {
	log.Println("========================================")
	log.Println("Received S3 JSON file event")
	log.Printf("Event Type: %s\n", e.Type())
	log.Printf("Event Source: %s\n", e.Source())
	log.Printf("Event Subject (filename): %s\n", e.Subject())
	log.Printf("Event ID: %s\n", e.ID())
	log.Printf("Event Time: %s\n", e.Time())

	var rawData []byte
	if err := e.DataAs(&rawData); err != nil {
		log.Printf("Error extracting raw data: %v\n", err)
		return nil, err
	}

	log.Println("----------------------------------------")
	log.Println("File Content (raw):")
	log.Printf("%s\n", string(rawData))
	log.Println("----------------------------------------")

	var jsonData interface{}
	if err := json.Unmarshal(rawData, &jsonData); err == nil {
		prettyJSON, _ := json.MarshalIndent(jsonData, "", "  ")
		log.Println("File Content (formatted JSON):")
		log.Printf("%s\n", string(prettyJSON))
	} else {
		log.Printf("Note: Content is not valid JSON: %v\n", err)
	}

	log.Println("========================================")
	return nil, nil
}
