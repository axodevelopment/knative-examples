package function

import (
	"context"
	"fmt"

	"github.com/cloudevents/sdk-go/v2/event"
)

// Handle an event.
func Handle(ctx context.Context, e event.Event) (*event.Event, error) {
	log.Println("========================================")
    log.Println("Received S3 JSON file event")
    log.Printf("Event Type: %s\n", ce.Type())
    log.Printf("Event Source: %s\n", ce.Source())
    log.Printf("Event Subject (filename): %s\n", ce.Subject())
    log.Printf("Event ID: %s\n", ce.ID())
    log.Printf("Event Time: %s\n", ce.Time())
    
    // Get the raw data from the CloudEvent
    var rawData []byte
    if err := ce.DataAs(&rawData); err != nil {
        log.Printf("Error extracting raw data: %v\n", err)
        return nil, err
    }
    
    // Log the raw content
    log.Println("----------------------------------------")
    log.Println("File Content (raw):")
    log.Printf("%s\n", string(rawData))
    log.Println("----------------------------------------")
    
    // Try to parse as JSON for pretty printing
    var jsonData interface{}
    if err := json.Unmarshal(rawData, &jsonData); err == nil {
        prettyJSON, _ := json.MarshalIndent(jsonData, "", "  ")
        log.Println("File Content (formatted JSON):")
        log.Printf("%s\n", string(prettyJSON))
    } else {
        log.Printf("Note: Content is not valid JSON: %v\n", err)
    }
    
    log.Println("========================================")
    
    // Return nil to acknowledge the event without sending a response
    return nil, nil

	fmt.Println("Received event")
	fmt.Println(e) // echo to local output
	return &e, nil // echo to caller
}

/*
Other supported function signatures:

	Handle()
	Handle() error
	Handle(context.Context)
	Handle(context.Context) error
	Handle(event.Event)
	Handle(event.Event) error
	Handle(context.Context, event.Event)
	Handle(context.Context, event.Event) error
	Handle(event.Event) *event.Event
	Handle(event.Event) (*event.Event, error)
	Handle(context.Context, event.Event) *event.Event
	Handle(context.Context, event.Event) (*event.Event, error)

*/
