package window

import "log"

func (w *Window) AddMouseChangeCallback(cb func(uint, uint)) (callbackId int) {
	id := w.mouseChangeCallbackId
	w.mouseChangeCallbacks[id] = cb
	w.mouseChangeCallbackId++
	log.Println("[window] mouse change callback added:", id)
	return id
}

func (w *Window) RemoveMouseChangeCallback(id int) {
	delete(w.mouseChangeCallbacks, id)
	log.Println("[window] mouse change callback deleted:", id)

}

var laterJobs []func()

// RunLater queues provided job to be run in the next frame.
func RunLater(job func()) {
	laterJobs = append(laterJobs, job)
}

var repeatJobs []func()

// RunRepeat stores provided job in a separate slice to run it in all other frames.
// Unlike the RunLater job will be executed all the time until the application shut down.
func RunRepeat(job func()) {
	repeatJobs = append(repeatJobs, job)
}
