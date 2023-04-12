package req

import (
	"fmt"
	"io"
	"net/http"
)

func Get(url string) (body []byte, err error) {
	req, err := http.NewRequest(http.MethodGet, url, nil)
	if err != nil {
		return nil, fmt.Errorf("fail to create a request to [%s]: %v", url, err)
	}

	client := http.Client{}
	resp, err := client.Do(req)
	if err != nil {
		return nil, fmt.Errorf("fail to get a response from [%s]: %v", url, err)
	}

	if resp.StatusCode < 200 || resp.StatusCode > 299 {
		return nil, fmt.Errorf("fail to get successfull response: code [%d]", resp.StatusCode)
	}

	if body, err = io.ReadAll(resp.Body); err != nil {
		return nil, fmt.Errorf("fail to read remote data: %v", err)
	}

	if err := resp.Body.Close(); err != nil {
		return nil, fmt.Errorf("fail to close response: %v", err)
	}

	return body, nil
}
