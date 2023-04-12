package keygen

import (
	"log"
	"math"
	"math/rand"

	"sdmm/internal/dmapi/dmmap/dmmdata"
)

const (
	base float64 = 52.0

	// We can only have three tiers of keys: https://secure.byond.com/forum/?post=2340796#comment23770802
	tier1limit = 51    // a-Z
	tier2limit = 2703  // aa-ZZ
	tier3limit = 65528 // aaa-ymi
)

var (
	// Calculated tiers with regard to 'a~' and 'a~~' keys.
	realTier1limit = tier1limit
	realTier2limit = tier1limit + tier2limit + 1
	realTier3limit = realTier2limit + tier3limit + 1

	keys = make([]dmmdata.Key, 0, realTier3limit+1)

	base52keys = []rune{
		'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm',
		'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
		'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M',
		'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
	}
)

// Generate all possible keys.
func init() {
	generateKeysRange(0, tier1limit, 1)            // a-Z
	generateKeysRange(0, tier1limit, 2)            // aa-aZ
	generateKeysRange(tier1limit+1, tier2limit, 2) // ba-ZZ
	generateKeysRange(0, tier2limit, 3)            // aaa-aZZ
	generateKeysRange(tier2limit+1, tier3limit, 3) // baa-ymi
}

func generateKeysRange(min, max, length int) {
	for i := min; i <= max; i++ {
		keys = append(keys, num2key(i, length))
	}
}

func num2key(num, keyLength int) dmmdata.Key {
	currentNum := num
	var result []rune

	for {
		result = append([]rune{base52keys[currentNum%int(base)]}, result...)
		currentNum = int(math.Floor(float64(currentNum) / base))
		if currentNum <= 0 {
			break
		}
	}

	lengthDiff := keyLength - len(result)

	if lengthDiff != 0 {
		for i := 0; i < lengthDiff; i++ {
			result = append([]rune{'a'}, result...)
		}
	}

	return dmmdata.Key(result)
}

type KeyGen struct {
	data *dmmdata.DmmData

	keysPool []int
	freeKeys int
}

func New(data *dmmdata.DmmData) *KeyGen {
	return &KeyGen{
		data: data,
	}
}

func (k *KeyGen) DropKeysPool() {
	k.keysPool = nil
	log.Println("[keygen] keys pool dropped")
}

// CreateKey generates a random key.
// Returns two values: a new key and a new key length.
// The second one will come only in the case, when there is no free keys in the keys pool with the current size.
func (k *KeyGen) CreateKey() (dmmdata.Key, int) {
	if k.keysPool == nil {
		k.keysPool, k.freeKeys = createKeysPool(k.data)
	}

	// If the keys pool is empty, then we need to create a new one with a new key length.
	if len(k.keysPool) == 0 {
		switch k.freeKeys {
		case realTier1limit:
			return "", 2
		case realTier2limit:
			return "", 3
		default:
			return "", -1 // Unbelievable situation where there is no free key at all.
		}
	}

	// Pick a random key from the pool.
	randomIdx := rand.Intn(len(k.keysPool))
	randomKey := k.keysPool[randomIdx]
	k.keysPool = append(k.keysPool[:randomIdx], k.keysPool[randomIdx+1:]...)

	return keys[randomKey], 0
}

func createKeysPool(data *dmmdata.DmmData) (keysPool []int, freeKeys int) {
	var border int
	switch data.KeyLength {
	case 1:
		freeKeys = realTier1limit
	case 2:
		freeKeys = realTier2limit
		border = realTier1limit + 1
	case 3:
		freeKeys = realTier3limit
		border = realTier2limit + 1
	}

	for num := border; num <= freeKeys; num++ {
		if _, ok := data.Dictionary[keys[num]]; !ok {
			keysPool = append(keysPool, num)
		}
	}

	log.Println("[keygen] keys pool created")
	log.Println("[keygen] keys pool size:", len(keysPool))
	log.Println("[keygen] free keys tier:", freeKeys)

	return keysPool, freeKeys
}
