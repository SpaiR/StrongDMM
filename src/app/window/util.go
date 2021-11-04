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

func RunLater(job func()) {
	laterJobs = append(laterJobs, job)
}
